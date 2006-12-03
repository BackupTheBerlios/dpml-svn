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

package net.dpml.transit.console;

import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.CacheDirective;

/**
 * Utility class supporting mutation of a transit directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class TransitDirectiveBuilder 
{
    private final TransitDirective m_directive;
    
    TransitDirectiveBuilder( TransitDirective directive )
    {
        m_directive = directive;
    }
    
    TransitDirective create( CacheDirective cache )
    {
        return create( null, cache );
    }
    
    TransitDirective create( ProxyDirective proxy )
    {
        return create( proxy, null );
    }
    
    TransitDirective create( ProxyDirective proxy, CacheDirective cache )
    {
        return new TransitDirective( 
          getProxyDirective( proxy ),
          getCacheDirective( cache ) );
    }
    
    ProxyDirective getProxyDirective( ProxyDirective proxy )
    {
        if( null != proxy )
        {
            return proxy;
        }
        else
        {
            return m_directive.getProxyDirective();
        }
    }
    
    CacheDirective getCacheDirective( CacheDirective cache )
    {
        if( null != cache )
        {
            return cache;
        }
        else
        {
            return m_directive.getCacheDirective();
        }
    }
}

