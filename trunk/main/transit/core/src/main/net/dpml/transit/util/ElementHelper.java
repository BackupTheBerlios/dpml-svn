/*
 * Copyright 2004-2005 Stephen McConnell
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.transit.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class supporting the translation of DOM content into local child, children,
 * attribute and value values.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ElementHelper
{
    private ElementHelper()
    {
        // utility class
    }

   /**
    * Return the root element of the supplied file.
    * @param definition the file to load
    * @return the root element
    * @exception Exception if the error occurs during root element establishment
    */
    public static Element getRootElement( final File definition )
      throws Exception
    {
        if( !definition.exists() )
        {
            throw new FileNotFoundException( definition.toString() );
        }

        if( !definition.isFile() )
        {
            final String error =
              "Source is not a file: " + definition;
            throw new IllegalArgumentException( error );
        }

        final DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        factory.setValidating( false );
        factory.setNamespaceAware( false );
        final Document document =
          factory.newDocumentBuilder().parse( definition );
        return document.getDocumentElement();
    }

   /**
    * Return the root element of the supplied input stream.
    * @param input the input stream containing a XML definition
    * @return the root element
    * @exception Exception if an error occurs
    */
    public static Element getRootElement( final InputStream input )
      throws Exception
    {
        final DocumentBuilderFactory factory =
          DocumentBuilderFactory.newInstance();
        factory.setValidating( false );
        factory.setNamespaceAware( false );
        factory.setExpandEntityReferences( false );
        DocumentBuilder builder = factory.newDocumentBuilder();
        return getRootElement( builder, input );
    }

   /**
    * Return the root element of the supplied input stream.
    * @param builder the document builder
    * @param input the input stream containing a XML definition
    * @return the root element
    * @exception Exception if an error occurs
    */
    public static Element getRootElement( final DocumentBuilder builder, final InputStream input )
      throws Exception
    {
        final Document document = builder.parse( input );
        return document.getDocumentElement();
    }

   /**
    * Return a named child relative to a supplied element.
    * @param root the parent DOM element
    * @param name the name of a child element
    * @return the child element of null if the child does not exist
    */
    public static Element getChild( final Element root, final String name )
    {
        if( null == root )
        {
            return null;
        }
        final NodeList list = root.getElementsByTagName( name );
        final int n = list.getLength();
        if( n < 1 )
        {
            return null;
        }
        return (Element) list.item( 0 );
    }

   /**
    * Return all children matching the supplied element name.
    * @param root the parent DOM element
    * @param name the name against which child element will be matched
    * @return the array of child elements with a matching name
    */
    public static Element[] getChildren( final Element root, final String name )
    {
        if( null == root )
        {
            return new Element[0];
        }
        final NodeList list = root.getElementsByTagName( name );
        final int n = list.getLength();
        final ArrayList result = new ArrayList();
        for( int i=0; i < n; i++ )
        {
            final Node item = list.item( i );
            if( item instanceof Element )
            {
                result.add( item );
            }
        }
        return (Element[]) result.toArray( new Element[0] );
    }

   /**
    * Return all children of the supplied parent.
    * @param root the parent DOM element
    * @return the array of all children
    */
    public static Element[] getChildren( final Element root )
    {
        if( null == root )
        {
            return new Element[0];
        }
        final NodeList list = root.getChildNodes();
        final int n = list.getLength();
        if( n < 1 )
        {
            return new Element[0];
        }
        final ArrayList result = new ArrayList();
        for( int i=0; i < n; i++ )
        {
            final Node item = list.item( i );
            if( item instanceof Element )
            {
                result.add( item );
            }
        }
        return (Element[]) result.toArray( new Element[0] );
    }

   /**
    * Return the value of an element.
    * @param node the DOM node
    * @return the node value
    */
    public static String getValue( final Element node )
    {
        if( null == node )
        {
            return null;
        }
        String value;
        if( node.getChildNodes().getLength() > 0 )
        {
            value = node.getFirstChild().getNodeValue();
        }
        else
        {
            value = node.getNodeValue();
        }
        return normalize( value );
    }

   /**
    * Return the value of an element attribute.
    * @param node the DOM node
    * @param key the attribute key
    * @return the attribute value or null if the attribute is undefined
    */
    public static String getAttribute( final Element node, final String key )
    {
        return getAttribute( node, key, null );
    }

   /**
    * Return the value of an element attribute.
    * @param node the DOM node
    * @param key the attribute key
    * @param def the default value if the attribute is undefined
    * @return the attribute value or the default value if undefined
    */
    public static String getAttribute( final Element node, final String key, final String def )
    {
        if( null == node )
        {
            return def;
        }
        if( !node.hasAttribute( key ) )
        {
            return def;
        } 
        final String value = node.getAttribute( key );
        return normalize( value );
    }

   /**
    * Return the value of an element attribute as a boolean
    * @param node the DOM node
    * @param key the attribute key
    * @return the attribute value as a boolean or false if undefined
    */
    public static boolean getBooleanAttribute( final Element node, final String key )
    {
        return getBooleanAttribute( node, key, false );
    }

   /**
    * Return the value of an element attribute as a boolean.
    * @param node the DOM node
    * @param key the attribute key
    * @param def the default value if the attribute is undefined
    * @return the attribute value or the default value if undefined
    */
    public static boolean getBooleanAttribute( final Element node, final String key, final boolean def )
    {
        if( null == node )
        {
            return def;
        }

        if( !node.hasAttribute( key ) )
        {
            return def;
        } 

        String value = node.getAttribute( key );
        value = normalize( value );
        if( value.equals( "" ) )
        {
            return def;
        }
        if( value.equalsIgnoreCase( "true" ) )
        {
            return true;
        }
        if( value.equalsIgnoreCase( "false" ) )
        {
            return false;
        }
        final String error =
          "Boolean argument [" + value + "] not recognized.";
        throw new IllegalArgumentException( error );
    }

   /**
    * Parse the value for any property tokens relative to system properties.
    * @param value the value to parse
    * @return the normalized string
    */
    static String normalize( String value )
    {
        return normalize( value, System.getProperties() );
    }

   /**
    * Parse the value for any property tokens relative to the supplied properties.
    * @param value the value to parse
    * @param props the reference properties
    * @return the normalized string
    */
    static String normalize( String value, Properties props )
    {
        return PropertyResolver.resolve( props, value );
    }
}
