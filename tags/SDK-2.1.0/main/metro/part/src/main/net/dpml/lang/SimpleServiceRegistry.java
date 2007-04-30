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

/**
 * Simple service registry implementation that selects the first service 
 * that is type assignable from the list of service instance supplied to 
 * the registry constructor.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SimpleServiceRegistry implements ServiceRegistry
{
    private final ServiceRegistry m_parent;
    private final Object[] m_args;
    
   /**
    * Creation of a new service registry with no parent.
    * @param args an ordered sequence of thread-safe services
    */
    public SimpleServiceRegistry( Object... args )
    {
       this( null, args );
    }
    
   /**
    * Creation of a new service registry with no parent.
    * @param parent an optional fallback registry
    * @param args an ordered sequence of thread-safe services
    */
    public SimpleServiceRegistry( ServiceRegistry parent, Object... args )
    {
        if( null == args )
        {
            throw new NullPointerException( "args" );
        }
        m_parent = parent;
        m_args = args;
    }
    
   /**
    * Matches a service in the registry with the supplied type.  If no
    * match is found and a parent registry has been delclared, the lookup
    * request is passed onto the parent registry, otherwise null is 
    * returned.
    * @param type the service type
    * @return an instance of the type or null if the type could not be resolved
    */
    public <T>T lookup( Class<T> type )
    {
        for( Object object : m_args )
        {
            if( null != object )
            {
                Class c = object.getClass();
                if( type.isAssignableFrom( c ) )
                {
                    return type.cast( object );
                }
            }
        }
        if( null != m_parent )
        {
            return m_parent.lookup( type );
        }
        return null;
    }
}

