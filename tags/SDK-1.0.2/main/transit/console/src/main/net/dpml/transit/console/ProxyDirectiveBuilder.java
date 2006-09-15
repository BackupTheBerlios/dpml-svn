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

import java.net.URL;

import net.dpml.transit.info.ProxyDirective;

/**
 * Utuility class supporting mutation of a proxy directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ProxyDirectiveBuilder 
{
    private final ProxyDirective m_directive;
    
    ProxyDirectiveBuilder( ProxyDirective directive )
    {
        m_directive = directive;
    }
    
    ProxyDirective create( URL host, String[] excludes, String username, char[] password )
    {
        return new ProxyDirective( 
          getHost( host ),
          getExcludes( excludes ),
          getUsername( username ),
          getPassword( password ) );
    }
    
    String getHost( URL host )
    {
        if( null != host )
        {
            return host.toString();
        }
        else
        {
            return m_directive.getHost();
        }
    }
    
    String getUsername( String username )
    {
        if( null != username )
        {
            return username;
        }
        else
        {
            return m_directive.getUsername();
        }
    }
    
    char[] getPassword( char[] password )
    {
        if( null != password )
        {
            return password;
        }
        else
        {
            return m_directive.getPassword();
        }
    }

    String[] getExcludes( String[] excludes )
    {
        if( null != excludes )
        {
            return excludes;
        }
        else
        {
            return m_directive.getExcludes();
        }
    }
}

