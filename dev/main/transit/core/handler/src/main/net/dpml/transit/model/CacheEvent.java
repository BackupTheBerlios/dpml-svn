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
 * An event related to the addition or removal of a host.
 */
public abstract class CacheEvent extends EventObject
{
    private final HostModel m_host;

    public CacheEvent( CacheModel source, HostModel host )
    {
        super( source );
        m_host = host;
    }
    
    public CacheModel getCacheModel()
    {
        return (CacheModel) getSource();
    }

    public HostModel getHostModel()
    {
        return m_host;
    }
}
