/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.lang;

import java.util.ServiceLoader;

/**
 * Default service registry implementation that resolves services via 
 * services resolvable using <tt>java.util.ServiceLoader</tt>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardServiceRegistry implements ServiceRegistry
{
    private final ServiceRegistry m_parent;
    
   /**
    * Creation of a new service registry with no parent.
    */
    public StandardServiceRegistry()
    {
        this( null );
    }
    
   /**
    * Creation of a new service registry with fallback delegation to
    * a parent registry.
    *
    * @param parent an optional parent registry
    */
    public StandardServiceRegistry( ServiceRegistry parent )
    {
        m_parent = parent;
    }
    
   /**
    * Matches a service in the registry with the supplied type.  If no
    * match is found and a parent registry has been delclared, the lookup
    * request is passed onto the parent registry, otherwise null is 
    * returned.
    */
    public <T>T lookup( Class<T> type )
    {
        ServiceLoader<T> loaders = ServiceLoader.load( type );
        for( T service : loaders )
        {
            return service; // the first available
        }
        if( null != m_parent )
        {
            return m_parent.lookup( type );
        }
        return null;
    }
}

