/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.avalon.util.configuration;

import java.util.ArrayList;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

/**
 * General utility supporting static operations for generating string
 * representations of a configuration suitable for debugging.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ConfigurationUtil
{
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
     * @return a simplified text representation of a configuration suitable
     *     for debugging
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
        return org.apache.avalon.framework.configuration.ConfigurationUtil.equals(c1, c2 );
    }
}
