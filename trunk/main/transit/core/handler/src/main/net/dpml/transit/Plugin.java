/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

import java.io.Serializable;
import java.net.URI;

/**
 * A Plugin class contains immutable data about a plugin based on a descriptor resolved
 * from a 'plugin' artifact.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Plugin.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public interface Plugin
{
   /**
    * Key used to designate an api classloader entry.
    */
    static final Category ANY = new Category( -1 );

   /**
    * Key used to designate an api classloader entry.
    */
    static final Category SYSTEM = new Category( 0 );

   /**
    * Key used to designate an api classloader entry.
    */
    static final Category API = new Category( 1 );

   /**
    * Key used to designate an spi classloader entry.
    */
    static final Category SPI = new Category( 2 );

   /**
    * Key used to designate an impl classloader entry.
    */
    static final Category IMPL = new Category( 3 );

   /**
    * Return the uri to the plugin descriptor.
    * @return the plugin uri
    */
    URI getURI();

   /**
    * Return the plugin data specification namespace value.
    * @return the namespace
    */
    String getSpecificationNamespace();

   /**
    * Return the plugin data version
    * @return the version
    */
    String getSpecificationVersion();

   /**
    * Return the classname.
    * @return the classname
    */
    String getClassname();

   /**
    * Return the factory interface.
    * @return the interface classname
    */
    String getInterface();

   /**
    * Return the implementation dependencies
    *
    * @param key SYSTEM, API, SPI and IMPL constants.
    *
    * @return the uris matching the key
    */
    URI[] getDependencies( Category key );

   /**
    * Return the aggregated set of dependency uris.
    *
    * @return the dependency artifacts
    */
    URI[] getDependencies();

    //==========================================================================
    // supplimentary data for antlib handling
    //==========================================================================

   /**
    * Return the antlib resource path.
    * @return the path
    */
    String getResource();

   /**
    * Return the antlib urn
    * @return the urn
    */
    String getURN();

   /**
    * Classloader category enumeration.
    */
    public static class Category implements Serializable
    {
        private final int m_index;

       /**
        * Internal constructor.
        * @param index the enumeration index.
        */
        private Category( int index )
        {
            m_index = index;
        }

       /**
        * Test this category for equality with the supplied instance.
        * @param other the object to test against
        * @return true if the instances are equivalent
        */
        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            else if( other.getClass() == Category.class )
            {
                Category key = (Category) other;
                return key.m_index == m_index;
            }
            else
            {
                return false;
            }
        }

       /**
        * Return the hascode for this instance.
        * @return the instance hashcode
        */
        public int hashCode()
        {
            return m_index;
        }
    }

}
