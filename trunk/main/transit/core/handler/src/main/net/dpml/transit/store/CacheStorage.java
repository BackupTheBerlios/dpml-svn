/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.store;

import java.io.File;

/**
 * Interface implemented by classes maintaining a cache configuration.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface CacheStorage extends CodeBaseStorage
{
   /**
    * Return the cache directory path assigned to the cache storage unit.
    * @return the cache directory path
    */
    String getCacheDirectoryPath();

   /**
    * Set the cache directory path to the supplied value.  The value may contain 
    * symbolic values in the form <code>${variable}</code> where varaible is the 
    * name of an existing system property.
    *
    * @param path a symbolic path
    */
    void setCacheDirectoryPath( String path );

   /**
    * Return the layout model key assiciated with the cache storage unit.
    * @return the cache storage layout key
    */
    String getLayoutModelKey();

   /**
    * Set the layout for the cache.
    * @param key the layout key identifier
    */
    void setLayoutModelKey( String key );
}
