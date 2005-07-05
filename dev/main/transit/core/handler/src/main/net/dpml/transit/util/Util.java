/*
 * Copyright 2004 Niclas Hedhman
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

import net.dpml.transit.Transit;

import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Utility class supporting operations related to property retrival.
 */
public final class Util
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * Read a set of properties from a property file specificed by a url.
    * Property files may reference symbolic properties in the form ${name}.
    * @param propsUrl the url of the property file to read
    * @return the resolved properties
    * @exception IOException if an io error occurs
    */
    public static Properties readProps( URL propsUrl )
        throws IOException
    {
         return readProps( propsUrl, true );
    }

   /**
    * Read a set of properties from a property file specificed by a url.
    * Property files may reference symbolic properties in the form ${name}.
    * @param propsUrl the url of the property file to read
    * @return the resolved properties
    * @exception IOException if an io error occurs
    */
    public static Properties readProps( URL propsUrl, boolean resolve )
        throws IOException
    {
        InputStream stream = propsUrl.openStream();
        try
        {
            Properties p = new Properties();
            p.load( stream );
            if( resolve )
            {
                p.setProperty( Transit.HOME_KEY, Transit.DPML_HOME.toString() );
                Iterator list = p.entrySet().iterator();
                while ( list.hasNext() )
                {
                    Map.Entry entry = (Map.Entry) list.next();
                    String value = (String) entry.getValue();
                    value = resolveProperty( p, value );
                    entry.setValue( value );
                }
            }
            return p;
        } 
        finally
        {
            stream.close();
        }
    }

   /**
    * Resolve symbols in a supplied value against supplied known properties.
    * @param props a set of know properties
    * @param value the string to parse for tokens
    * @return the resolved string
    */
    public static String resolveProperty( Properties props, String value )
    {
        value =  PropertyResolver.resolve( props, value );
        return value;
    }

   /**
    * Return the value of a property.
    * @param props the property file
    * @param key the property key to lookup
    * @param def the default value
    * @return the resolve value
    */
    public static String getProperty( Properties props, String key, String def )
    {
        String value = props.getProperty( key, def );
        if( value == null )
        {
            return null;
        }
        if( "".equals( value ) )
        {
            return value;
        }
        value =  PropertyResolver.resolve( props, value );
        return value;
    }

   /**
    * Read a file and return the list of lines in an array of strings.
    * @param listFile the url to read from
    * @return the lines
    * @exception IOException if a read error occurs
    */
    public static String[] readListFile( URL listFile )
        throws IOException
    {
        ArrayList list = new ArrayList();
        InputStream stream = listFile.openStream();
        try
        {
            InputStreamReader isr = new InputStreamReader( stream, "UTF-8" );
            BufferedReader reader = new BufferedReader( isr );
            String line = reader.readLine();
            while ( line != null )
            {
                list.add( line );
                line = reader.readLine();
            }
            String[] items = new String[ list.size() ];
            list.toArray( items );
            return items;
        } 
        finally
        {
            stream.close();
        }
    }

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Constructor.
    */
    private Util()
    {
    }
}
