/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.util.defaults ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Properties ;
import java.util.Enumeration ;


/**
 * Gets a set of default property values based on a sequence of default value
 * search components or finders.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Defaults.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Defaults extends Properties
{
    /** single-valued property key names */ 
    private final String [] m_singles ;
    /** multi-valued or enumerated property key names */  
    private final String [] m_enumerated ; 
    /** array of finders that define the property default discovery process */
    private final DefaultsFinder [] m_finders ;
    
    
    // ------------------------------------------------------------------------
    // C O N S T R U C T O R S
    // ------------------------------------------------------------------------

    
    /**
     * Creates and populates a set of properties
     * 
     * @param a_singles single valued key names
     * @param a_enumerated multi-valued key names
     * @param a_finders search components used for staged discovery of defaults
     */
    public Defaults( String [] a_singles, String [] a_enumerated, 
                     DefaultsFinder [] a_finders )
    {
        m_finders = a_finders ;
        m_singles = a_singles ;
        m_enumerated = a_enumerated ;
        
        for ( int ii = 0; ii < m_finders.length; ii++ )
        {
            m_finders[ii].find( this ) ;
        }
    }

    
    // ------------------------------------------------------------------------
    // A C C E S S O R S 
    // ------------------------------------------------------------------------
    
    
    /**
     * Gets the base names of enumerated multi-valued keys.  Such keys are 
     * enumerated to have multiple values by appending an index onto a key base
     * like so: [base.key].1,[base.key].2,[base.key].3 ... [base.key].N.  The
     * returned keys are just the base key names of multi-valued properties and
     * do not include the appended index.
     * 
     * @return the base key names for multi-valued properties
     */
    public String[] getEnumerated()
    {
        return m_enumerated ;
    }

    
    /**
     * Gets the linear set of finders composing the search policy.
     * 
     * @return the finders used to discover property defaults
     */
    public DefaultsFinder[] getFinders()
    {
        return m_finders ;
    }


    /**
     * Gets the names of all the single valued properties. 
     * 
     * @return single valued property key names
     */
    public String[] getSingles()
    {
        return m_singles ;
    }
    
    
    /**
     * Gets the default values for an enumerated key.
     * 
     * @param a_base the base of the enumerated key
     * @return the values of the multi-valued property
     */
    public String[] getEnumerated( String a_base )
    {
        ArrayList l_values = new ArrayList() ;
        Enumeration l_list = keys() ;

        while ( l_list.hasMoreElements() )
        {
            String l_key = ( String ) l_list.nextElement() ;
            if ( l_key.startsWith( a_base ) )
            {
                l_values.add( getProperty( l_key ) ) ;
            }
        }
        return ( String [] ) l_values.toArray( new String [0] ) ;
    }

    
    /**
     * Utility method that gets a key's value and returns a boolean value to 
     * represent it.
     * 
     * @param a_key the boolean property key
     * @return true if the property is 1, true, yes or on, and false otherwise 
     */
    public boolean getBoolean( String a_key )
    {
        String l_value = getProperty( a_key ) ;
        l_value = l_value.trim().toLowerCase() ;
        
        if ( l_value.equals( "1" )       ||
             l_value.equals( "on" )      ||
             l_value.equals( "yes" )     ||
             l_value.equals( "true" ) )
        {
            return true ;
        }
        
        return false ;
    }
    
    
    // ------------------------------------------------------------------------
    // S T A T I C   M E T H O D S
    // ------------------------------------------------------------------------
    
    
    /**
     * Merges a set of properties from source Properties into a Defaults 
     * instance.  Does not allow null overrides.
     * 
     * @param a_defaults the defaults to populate on discovery
     * @param a_sources the sources to search
     * @param a_haltOnDiscovery true to halt on first find or false to continue
     * to last find
     */
    public static void discover( Defaults a_defaults, Properties [] a_sources,
                                 boolean a_haltOnDiscovery )
    {
        if ( null == a_sources || null == a_defaults )
        {
            return ;
        }
        
        /*
         * H A N D L E   S I N G L E   V A L U E D   K E Y S  
         */
        String [] l_keys = a_defaults.getSingles() ;
        for ( int ii = 0; ii < l_keys.length; ii++ )
        {
            String l_key = l_keys[ii] ;
            String l_value = discover( l_key, a_sources, a_haltOnDiscovery ) ;
            
            if ( l_value != null )
            {
                a_defaults.setProperty( l_key, l_value ) ;
            }
        }
        
        /*
         * H A N D L E   M U L T I - V A L U E D   K E Y S 
         */
        l_keys = a_defaults.getEnumerated() ;
        for ( int ii = 0; ii < l_keys.length; ii++ )
        {
            String l_base = l_keys[ii] ;
            
            for ( int jj = 0; jj < a_sources.length; jj++ )
            {
                Enumeration l_list = a_sources[jj].propertyNames() ;
                
                while ( l_list.hasMoreElements() )
                {
                    String l_key = ( String ) l_list.nextElement() ;
                    if ( ! l_key.startsWith( l_base ) )
                    {
                        continue ;
                    }
                    
                    String l_value = 
                        discover( l_key, a_sources, a_haltOnDiscovery ) ;

                    if ( l_value != null )
                    {
                        a_defaults.setProperty( l_key, l_value ) ;
                    }
                }
            }
        }
    }
    
    
    /**
     * Discovers a value within a set of Properties either halting on the first
     * time the property is discovered or continuing on to take the last value
     * found for the property key.
     * 
     * @param l_key a property key
     * @param a_sources a set of source Properties
     * @param a_haltOnDiscovery true if we stop on finding a value, false 
     * otherwise
     * @return the value found or null
     */
    public static String discover( String l_key, Properties [] a_sources,
                                   boolean a_haltOnDiscovery )
    {
        String l_retval = null ;
        
        for( int ii = 0; ii < a_sources.length; ii++ )
        {
            if ( a_sources[ii].containsKey( l_key ) )
            {
                l_retval = a_sources[ii].getProperty( l_key ) ;
                
                if ( a_haltOnDiscovery )
                {
                    break ;
                }
            }
        }
        
        return l_retval ;
    }


    /**
     * Expands out a set of property key macros in the following format 
     * ${foo.bar} where foo.bar is a property key, by dereferencing the value 
     * of the key using the original source Properties and other optional 
     * Properties.
     * 
     * If the original expanded Properties contain the value for the macro key 
     * foo.bar then dereferencing stops by using the value in the expanded 
     * Properties: the other optional Properties are NOT used at all.
     * 
     * If the original expanded Properties do NOT contain the value for the 
     * macro key, then the optional Properties are used in order.  The first of
     * the optionals to contain the value for the macro key (foo.bar) shorts the
     * search.  Hence the first optional Properties in the array to contain a 
     * value for the macro key (foo.bar) is used to set the expanded value.
     * 
     * If a macro cannot be expanded because it's key was not defined within the 
     * expanded Properties or one of the optional Properties then it is left as
     * is.
     * 
     * @param a_expanded the Properties to perform the macro expansion upon
     * @param a_optionals null or an optional set of Properties to use for 
     * dereferencing macro keys (foo.bar)
     */
    public static void macroExpand( Properties a_expanded, 
                                    Properties [] a_optionals )
    {
        // Handle null optionals
        if ( null == a_optionals )
        {
            a_optionals = new Properties [ 0 ] ;
        }
        
        Enumeration l_list = a_expanded.propertyNames() ;
        while ( l_list.hasMoreElements() )
        {
            String l_key = ( String ) l_list.nextElement() ;
            String l_macro = a_expanded.getProperty( l_key ) ;
            
            int n = l_macro.indexOf( "${" );
            if( n < 0 )
            {
                continue;
            }

            int m = l_macro.indexOf( "}", n+2 );
            if( m < 0 )
            {
                continue;
            }

            final String symbol = l_macro.substring( n+2, m );
            
            if ( a_expanded.containsKey( symbol ) )
            {
                final String value = a_expanded.getProperty( symbol );
                final String head = l_macro.substring( 0, n );
                final String tail = l_macro.substring( m+1 );
                final String resolved = head + value + tail;

                a_expanded.put( l_key, resolved ) ;
                continue ;
            }

            /*
             * Check if the macro key exists within the array of optional 
             * Properties.  Set expanded value to first Properties with the 
             * key and break out of the loop.
             */
            for ( int ii = 0; ii < a_optionals.length; ii++ )
            {
                if ( a_optionals[ii].containsKey( symbol ) )
                {
                    String value = a_optionals[ii].getProperty( symbol ) ;
                    final String head = l_macro.substring( 0, n );
                    final String tail = l_macro.substring( m+1 );
                    final String resolved = head + value + tail;

                    a_expanded.put( l_key, resolved ) ;
                    break ;
                }
            }
        }
    }

   /**
    * Read in a static properties resource relative to a supplied class.
    * The implementation will attempt to locate a property file colocated
    * with the class with the name [class].properties.
    *
    * @param ref a class used to establish a classloader and anchors 
    *    relative path references
    * @return the static properties
    */
    public static Properties getStaticProperties( Class ref ) throws IOException
    {
        final Properties properties = new Properties();
        final String address = ref.toString().replace( '.','/' );
        final String path = address + ".properties";
        InputStream input = ref.getResourceAsStream( path );
        if( null != input )
        {
            properties.load( input );
        }
        return properties;
    }

   /**
    * Read in a static properties resource relative to a supplied class
    * and path.
    *
    * @param ref a class used to establish a classloader and anchors 
    *    relative path references
    * @param path the resoruce address
    * @return the static properties
    * @exception IllegalStateException if the path is unresolvable
    */
    public static Properties getStaticProperties( Class ref, String path ) throws IOException
    {
        Properties bootstrap = new Properties();
        InputStream input = ref.getResourceAsStream( path );
        if( input == null )
        {
            final String error = 
              "Internal error, unable to locate enbedded resource: " 
              + path 
              + " from the resource: " 
              + ref.getProtectionDomain().getCodeSource().getLocation();
            throw new IllegalStateException( error );
        }
        bootstrap.load( input );
        return bootstrap;
    }
}


