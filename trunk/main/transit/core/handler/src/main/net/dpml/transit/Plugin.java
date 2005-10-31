/*
 * Copyright 2004-2005 Stephen J. McConnell.
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
import java.util.Hashtable;
import java.util.Map;

import net.dpml.transit.util.ValuedEnum;

/**
 * A Plugin class contains immutable data about a plugin based on a descriptor resolved
 * from a 'plugin' artifact.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Plugin
{
   /**
    * Key used to designate an system classloader category.
    */
    //static final Category SYSTEM = new Category( "system", 0, Category.m_table );

   /**
    * Key used to designate the API classloader category.
    */
    //static final Category API = new Category( "api", 1, Category.m_table );

   /**
    * Key used to designate an SPI classloader category.
    */
    //static final Category SPI = new Category( "spi", 2, Category.m_table );

   /**
    * Key used to designate an IMPL classloader category.
    */
   // static final Category IMPL = new Category( "impl", 3, Category.m_table );

   /**
    * Key used to designate any classloader category.
    */
    //static final Category ANY = new Category( "any", 4, null );

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
    //public static class Category extends ValuedEnum
    //{
    //    private static Map m_table = new Hashtable();
    // 
    //   /**
    //    * Returns an array of category values.
    //    * @return the category value array
    //    */
    //    public static Category[] values()
    //    {
    //        return (Category[]) m_table.values().toArray( new Category[0] );
    //    }
    //    
    //   /**
    //    * Internal constructor.
    //    * @param label the enumeration label.
    //    * @param index the enumeration index.
    //    * @param map the set of constructed enumerations.
    //    */
    //    private Category( String label, int index, Map map )
    //    {
    //        super( label, index, map );
    //    }
    //}
}
