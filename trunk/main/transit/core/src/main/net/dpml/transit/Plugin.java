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

import java.net.URI;

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
    * @param key NATIVE, SYSTEM, PRIVATE, PROTECTED or PUBLIC constants.
    *
    * @return the uris matching the key
    */
    URI[] getDependencies( Category key );

   /**
    * Return the native dependencies
    *
    * @return the uris to native libraries
    */
    URI[] getNativeDependencies();

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
}
