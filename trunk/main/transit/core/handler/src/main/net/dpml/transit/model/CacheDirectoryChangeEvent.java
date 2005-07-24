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

package net.dpml.transit.model;

import java.io.File;
import java.util.EventObject;

/**
 * Event signalling a change to the Tranist cache directory.
 */
public class CacheDirectoryChangeEvent extends EventObject
{
    private final String m_path;

   /**
    * Creation of a new cache directory change event.
    * @param source the cache model initialing the change
    * @param path the new cache path value
    */
    public CacheDirectoryChangeEvent( CacheModel source, String path )
    {
        super( source );
        m_path = path;
    }

   /**
    * Return the source cache model that rased the event.
    * @return the source of the event
    */
    public CacheModel getCacheModel()
    {
        return (CacheModel) getSource();
    }
    
   /**
    * Return the new cache path.
    * @return the value assigned as the new cache path
    */
    public String getCachePath()
    {
        return m_path;
    }
}
