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

import net.dpml.transit.info.HostDirective;

/**
 * Utility class supporting mutation of a host directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class HostDirectiveBuilder 
{
    private final HostDirective m_directive;
    
    HostDirectiveBuilder( HostDirective directive )
    {
        m_directive = directive;
    }
    
    HostDirective create(
      int priority, URL host, String index, String username, char[] password, boolean enabled, 
      boolean trusted, String layout, String scheme, String prompt )
    {
        return new HostDirective( 
          m_directive.getID(),
          getPriority( priority ),
          getHost( host ),
          getIndex( index ),
          getUsername( username ),
          getPassword( password ),
          enabled, 
          trusted,
          getLayout( layout ),
          getScheme( scheme ),
          getPrompt( prompt ) );
    }
    
    int getPriority( int priority )
    {
        if( priority > -1 )
        {
            return priority;
        }
        else
        {
            return m_directive.getPriority();
        }
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

    String getIndex( String index )
    {
        if( null != index )
        {
            return index;
        }
        else
        {
            return m_directive.getIndex();
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

    String getScheme( String scheme )
    {
        if( null != scheme )
        {
            return scheme;
        }
        else
        {
            return m_directive.getScheme();
        }
    }
    
    String getPrompt( String prompt )
    {
        if( null != prompt )
        {
            return prompt;
        }
        else
        {
            return m_directive.getPrompt();
        }
    }
}

