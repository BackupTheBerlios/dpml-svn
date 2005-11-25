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

import java.util.ArrayList;

import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Utuility class supporting mutation of a cache directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class CacheDirectiveBuilder 
{
    private final CacheDirective m_directive;
    
    CacheDirectiveBuilder( CacheDirective directive )
    {
        m_directive = directive;
    }
    
    CacheDirective removeHostDirective( String key ) throws UnknownKeyException
    {
        HostDirective[] hosts = m_directive.getHostDirectives();
        ArrayList list = new ArrayList();
        for( int i=0; i<hosts.length; i++ )
        {
            HostDirective host = hosts[i];
            if( !host.getID().equals( key ) )
            {
                list.add( host );
            }
        }
        HostDirective[] newHosts = (HostDirective[]) list.toArray( new HostDirective[0] );
        if( newHosts.length == hosts.length )
        {
            throw new UnknownKeyException( key );
        }
        return create( newHosts );
    }
    
    CacheDirective removeContentDirective( String key ) throws UnknownKeyException
    {
        ContentDirective[] handlers = m_directive.getContentDirectives();
        ArrayList list = new ArrayList();
        for( int i=0; i<handlers.length; i++ )
        {
            ContentDirective handler = handlers[i];
            if( !handler.getID().equals( key ) )
            {
                list.add( handler );
            }
        }
        ContentDirective[] newHandlers = (ContentDirective[]) list.toArray( new ContentDirective[0] );
        if( newHandlers.length == handlers.length )
        {
            throw new UnknownKeyException( key );
        }
        return create( newHandlers );
    }

    CacheDirective removeLayoutDirective( String key ) throws UnknownKeyException
    {
        LayoutDirective[] handlers = m_directive.getLayoutDirectives();
        ArrayList list = new ArrayList();
        for( int i=0; i<handlers.length; i++ )
        {
            LayoutDirective handler = handlers[i];
            if( !handler.getID().equals( key ) )
            {
                list.add( handler );
            }
        }
        LayoutDirective[] newHandlers = (LayoutDirective[]) list.toArray( new LayoutDirective[0] );
        if( newHandlers.length == handlers.length )
        {
            throw new UnknownKeyException( key );
        }
        return create( newHandlers );
    }
    
    CacheDirective create( HostDirective[] hosts )
    {
        return create( null, null, null, null, hosts, null );
    }
    
    CacheDirective create( ContentDirective[] handlers )
    {
        return create( null, null, null, null, null, handlers );
    }
    
    CacheDirective create( LayoutDirective[] layouts )
    {
        return create( null, null, null, layouts, null, null );
    }
    
    CacheDirective create( String cache, String local, String layout )
    {
        return create( cache, local, layout, null,null, null );
    }
    
    CacheDirective create(
      String cache, String local, String layout, 
      LayoutDirective[] layouts, HostDirective[] hosts, ContentDirective[] handlers )
    {
        return new CacheDirective( 
          getCache( cache ),
          getLocal( local ),
          getLayout( layout ),
          getLayoutDirectives( layouts ),
          getHostDirectives( hosts ),
          getContentDirectives( handlers ) );
    }
    
    String getCache( String cache )
    {
        if( null != cache )
        {
            return cache;
        }
        else
        {
            return m_directive.getCache();
        }
    }

    String getLocal( String local )
    {
        if( null != local )
        {
            return local;
        }
        else
        {
            return m_directive.getLocal();
        }
    }

    String getLayout( String layout )
    {
        if( null != layout )
        {
            return layout;
        }
        else
        {
            return m_directive.getLayout();
        }
    }

    LayoutDirective[] getLayoutDirectives( LayoutDirective[] layouts )
    {
        if( null != layouts )
        {
            return layouts;
        }
        else
        {
            return m_directive.getLayoutDirectives();
        }
    }

    HostDirective[] getHostDirectives( HostDirective[] hosts )
    {
        if( null != hosts )
        {
            return hosts;
        }
        else
        {
            return m_directive.getHostDirectives();
        }
    }

    ContentDirective[] getContentDirectives( ContentDirective[] handlers )
    {
        if( null != handlers )
        {
            return handlers;
        }
        else
        {
            return m_directive.getContentDirectives();
        }
    }
}

