/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.configuration.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.dpml.configuration.Configuration;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class has a bunch of utility methods to work
 * with configuration objects.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ConfigurationUtil.java 795 2004-11-19 07:29:52Z niclas@apache.org $
 * @since 4.1.4
 */
public class ConfigurationUtil
{
    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationUtil()
    {
    }

    /**
     * Convert a DOM Element tree into a configuration tree.
     *
     * @param element the DOM Element
     * @return the configuration object
     */
    public static Configuration toConfiguration( final Element element )
    {
        final DefaultConfiguration configuration =
            new DefaultConfiguration( element.getNodeName(), "dom-created" );
        final NamedNodeMap attributes = element.getAttributes();
        final int length = attributes.getLength();
        for( int i = 0; i < length; i++ )
        {
            final Node node = attributes.item( i );
            final String name = node.getNodeName();
            final String value = node.getNodeValue();
            configuration.setAttribute( name, value );
        }

        boolean flag = false;
        String content = "";
        final NodeList nodes = element.getChildNodes();
        final int count = nodes.getLength();
        for( int i = 0; i < count; i++ )
        {
            final Node node = nodes.item( i );
            if( node instanceof Element )
            {
                final Configuration child = toConfiguration( (Element)node );
                configuration.addChild( child );
            }
            else if( node instanceof CharacterData )
            {
                final CharacterData data = (CharacterData)node;
                content += data.getData();
                flag = true;
            }
        }

        if( flag )
        {
            configuration.setValue( content );
        }

        return configuration;
    }

    /**
     * Convert a configuration tree into a DOM Element tree.
     *
     * @param configuration the configuration object
     * @return the DOM Element
     */
    public static Element toElement( final Configuration configuration )
    {
        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.newDocument();

            return createElement( document, configuration );
        }
        catch( final ParserConfigurationException pce )
        {
            throw new IllegalStateException( pce.toString() );
        }
    }

    /**
     * Serialize the configuration object to a String.  If an exception
     * occurs, the exception message will be returned instead.  This method is
     * intended to aid debugging; {@link
     * DefaultConfigurationSerializer#serialize(Configuration)} lets the caller
     * handle exceptions.
     *
     * @param configuration Configuration instance to serialize
     * @return a non-null String representing the <code>Configuration</code>,
     * or an error message.
     * @since 12 March, 2003
     */
    public static String toString( final Configuration configuration )
    {
        DefaultConfigurationSerializer ser = new DefaultConfigurationSerializer();
        try
        {
            return ser.serialize( configuration );
        }
        catch( Exception e )
        {
            return e.getMessage();
        }
    }

    /**
     * Test to see if two Configuration's can be considered the same. Name, value, attributes
     * and children are test. The <b>order</b> of children is not taken into consideration
     * for equality.
     *
     * @param c1 Configuration to test
     * @param c2 Configuration to test
     * @return true if the configurations can be considered equals
     */
    public static boolean equals( final Configuration c1, final Configuration c2 )
    {
        return c1.getName().equals( c2.getName() ) && areValuesEqual( c1, c2 ) &&
            areAttributesEqual( c1, c2 ) && areChildrenEqual( c1, c2 );
    }

    /**
     * Return true if the children of both configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the children of both configurations are equal.
     */
    private static boolean areChildrenEqual( final Configuration c1,
                                             final Configuration c2 )
    {
        final Configuration[] kids1 = c1.getChildren();
        final ArrayList kids2 = new ArrayList( Arrays.asList( c2.getChildren() ) );
        if( kids1.length != kids2.size() )
        {
            return false;
        }

        for( int i = 0; i < kids1.length; i++ )
        {
            if( !findMatchingChild( kids1[ i ], kids2 ) )
            {
                return false;
            }
        }

        return kids2.isEmpty() ? true : false;
    }

    /**
     * Return true if find a matching child and remove child from list.
     *
     * @param c the configuration
     * @param matchAgainst the list of items to match against
     * @return true if the found.
     */
    private static boolean findMatchingChild( final Configuration c,
                                              final ArrayList matchAgainst )
    {
        final Iterator i = matchAgainst.iterator();
        while( i.hasNext() )
        {
            if( equals( c, (Configuration)i.next() ) )
            {
                i.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Return true if the attributes of both configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the attributes of both configurations are equal.
     */
    private static boolean areAttributesEqual( final Configuration c1,
                                               final Configuration c2 )
    {
        final String[] names1 = c1.getAttributeNames();
        final String[] names2 = c2.getAttributeNames();
        if( names1.length != names2.length )
        {
            return false;
        }

        for( int i = 0; i < names1.length; i++ )
        {
            final String name = names1[ i ];
            final String value1 = c1.getAttribute( name, null );
            final String value2 = c2.getAttribute( name, null );
            if( !value1.equals( value2 ) )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if the values of two configurations are equal.
     *
     * @param c1 configuration1
     * @param c2 configuration2
     * @return true if the values of two configurations are equal.
     */
    private static boolean areValuesEqual( final Configuration c1,
                                           final Configuration c2 )
    {
        final String value1 = c1.getValue( null );
        final String value2 = c2.getValue( null );
        return ( value1 == null && value2 == null ) ||
            ( value1 != null && value1.equals( value2 ) );
    }

    /**
     * Create an DOM {@link Element} from a {@link Configuration}
     * object.
     *
     * @param document the DOM document
     * @param configuration the configuration to convert
     * @return the DOM Element
     */
    private static Element createElement( final Document document,
                                          final Configuration configuration )
    {
        final Element element = document.createElement( configuration.getName() );

        final String content = configuration.getValue( null );
        if( null != content )
        {
            final Text child = document.createTextNode( content );
            element.appendChild( child );
        }

        final String[] names = configuration.getAttributeNames();
        for( int i = 0; i < names.length; i++ )
        {
            final String name = names[ i ];
            final String value = configuration.getAttribute( name, null );
            element.setAttribute( name, value );
        }
        final Configuration[] children = configuration.getChildren();
        for( int i = 0; i < children.length; i++ )
        {
            final Element child = createElement( document, children[ i ] );
            element.appendChild( child );
        }
        return element;
    }

    /**
     * Returns a simple string representation of the the supplied configuration.
     * @param config a configuration
     * @return a simplified text representation of a configuration suitable
     *     for debugging
     */
    public static String list( Configuration config )
    {
        final StringBuffer buffer = new StringBuffer();
        list( buffer, "  ", config );
        buffer.append( "\n" );
        return buffer.toString();
    }

    /**
     * populates a string buffer with an XML representation of a supplied configuration.
     * @param buffer the string buffer
     * @param lead padding offset
     * @param config a configuration
     */
    public static void list( StringBuffer buffer, String lead, Configuration config )
    {

        buffer.append( "\n" + lead + "<" + config.getName() );
        String[] names = config.getAttributeNames();
        if( names.length > 0 )
        {
            for( int i = 0; i < names.length; i++ )
            {
                buffer.append( " "
                               + names[ i ] + "=\""
                               + config.getAttribute( names[ i ], "???" ) + "\"" );
            }
        }
        Configuration[] children = config.getChildren();
        if( children.length > 0 )
        {
            buffer.append( ">" );
            for( int j = 0; j < children.length; j++ )
            {
                list( buffer, lead + "  ", children[ j ] );
            }
            buffer.append( "\n" + lead + "</" + config.getName() + ">" );
        }
        else
        {
            try
            {
                String value = config.getValue();
                if( !value.equals( "" ) )
                {
                    buffer.append( ">" + value + "</" + config.getName() + ">" );
                }
                else
                {
                    buffer.append( "/>" );
                }
            }
            catch( Throwable ce )
            {
                buffer.append( "/>" );
            }
        }
    }

    /**
     * Return all occurance of a configuration child containing the supplied attribute name.
     * @param config the configuration
     * @param element the name of child elements to select from the configuration
     * @param attribute the attribute name to filter (null will match any attribute name)
     * @return an array of configuration instances matching the query
     */
    public static Configuration[] match( final Configuration config,
                                         final String element,
                                         final String attribute )
    {
        return match( config, element, attribute, null );
    }

    /**
     * Return occurance of a configuration child containing the supplied attribute name and value.
     * @param config the configuration
     * @param element the name of child elements to select from the configuration
     * @param attribute the attribute name to filter (null will match any attribute name )
     * @param value the attribute value to match (null will match any attribute value)
     * @return an array of configuration instances matching the query
     */
    public static Configuration[] match( final Configuration config,
                                         final String element,
                                         final String attribute,
                                         final String value )
    {
        final ArrayList list = new ArrayList();
        final Configuration[] children = config.getChildren( element );

        for( int i = 0; i < children.length; i++ )
        {
            if( null == attribute )
            {
                list.add( children[ i ] );
            }
            else
            {
                String v = children[ i ].getAttribute( attribute, null );

                if( v != null )
                {
                    if( ( value == null ) || v.equals( value ) )
                    {
                        // it's a match
                        list.add( children[ i ] );
                    }
                }
            }
        }

        return (Configuration[])list.toArray( new Configuration[ list.size() ] );
    }

    /**
     * Return the first occurance of a configuration child containing the supplied attribute name
     * and value or create a new empty configuration if no match found.
     * @param config the configuration
     * @param element the name of child elements to select from the configuration
     * @param attribute the attribute name to filter
     * @param value the attribute value to match (null will match any attribute value)
     * @return a configuration instances matching the query or empty configuration
     */
    public static Configuration matchFirstOccurance(
        Configuration config, String element, String attribute, String value )
    {
        return matchFirstOccurance( config, element, attribute, value, true );
    }

    /**
     * Return the first occurance of a configuration child containing the supplied attribute
     * name and value.  If the supplied creation policy if TRUE and no match is found, an
     * empty configuration instance is returned, otherwise a null will returned.
     * @param config the configuration
     * @param element the name of child elements to select from the configuration
     * @param attribute the attribute name to filter
     * @param value the attribute value to match (null will match any attribute value)
     * @param create the creation policy if no match
     * @return a configuration instances matching the query
     */
    public static Configuration matchFirstOccurance(
        Configuration config, String element, String attribute, String value, boolean create )
    {
        Configuration[] children = config.getChildren( element );
        for( int i = 0; i < children.length; i++ )
        {
            String v = children[ i ].getAttribute( attribute, null );
            if( v != null )
            {
                if( ( value == null ) || v.equals( value ) )
                {
                    // it's a match
                    return children[ i ];
                }
            }
        }

        return create ? new DefaultConfiguration( element, null ) : null;
    }

}
