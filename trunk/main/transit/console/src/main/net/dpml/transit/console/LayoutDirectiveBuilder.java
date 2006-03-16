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

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.transit.info.LayoutDirective;
import net.dpml.lang.ValueDirective;

/**
 * Utility class supporting mutation of a layout directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class LayoutDirectiveBuilder 
{
    private final LayoutDirective m_directive;
    
    LayoutDirectiveBuilder( LayoutDirective directive )
    {
        m_directive = directive;
    }
    
    LayoutDirective create(
      String title, URI codebase, ValueDirective[] parameters )
      throws URISyntaxException
    {
        return new LayoutDirective( 
          m_directive.getID(),
          getTitle( title ),
          getCodeBase( codebase ),
          getValueDirectives( parameters ) );
    }
    
    String getTitle( String title )
    {
        if( null != title )
        {
            return title;
        }
        else
        {
            return m_directive.getTitle();
        }
    }

    String getCodeBase( URI codebase )
    {
        if( null != codebase )
        {
            return codebase.toASCIIString();
        }
        else
        {
            return m_directive.getCodeBaseURISpec();
        }
    }

    ValueDirective[] getValueDirectives( ValueDirective[] values )
    {
        if( null != values )
        {
            return values;
        }
        else
        {
            return m_directive.getValueDirectives();
        }
    }
}

