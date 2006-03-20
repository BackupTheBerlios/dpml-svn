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

import net.dpml.transit.info.ContentDirective;
import net.dpml.lang.ValueDirective;

/**
 * Utility class supporting mutation of a content directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ContentDirectiveBuilder 
{
    private final ContentDirective m_directive;
    
    ContentDirectiveBuilder( ContentDirective directive )
    {
        m_directive = directive;
    }
    
    ContentDirective create(
      String title, URI codebase, ValueDirective[] parameters ) 
      throws URISyntaxException
    {
        return new ContentDirective( 
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

    URI getCodeBase( URI codebase )
    {
        if( null != codebase )
        {
            return codebase;
        }
        else
        {
            return m_directive.getCodeBaseURI();
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

