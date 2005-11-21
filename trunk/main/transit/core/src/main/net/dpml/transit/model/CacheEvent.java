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

import java.util.EventObject;

/**
 * An abstract event related to the addition and removal of hosts
 * to and from the cache model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class CacheEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final HostModel m_host;

   /**
    * Construction of a new cache change event.
    * @param source the cache model initiating the event
    * @param host the host model added or removed from the cache
    */
    public CacheEvent( CacheModel source, HostModel host )
    {
        super( source );
        m_host = host;
    }
    
   /**
    * Return the cache model that initiating the event.
    * @return the cache model
    */
    public CacheModel getCacheModel()
    {
        return (CacheModel) getSource();
    }

   /**
    * Return the host model that was added or removed.
    * @return the host model
    */
    public HostModel getHostModel()
    {
        return m_host;
    }
}
