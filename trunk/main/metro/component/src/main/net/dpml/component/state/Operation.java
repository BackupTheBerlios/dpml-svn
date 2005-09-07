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

package net.dpml.component.state;

import java.net.URI;
import java.io.Serializable;

/**
 * The Operation links an operation to a handler uri.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class Operation implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final URI m_handler;

   /**
    * Internal utility to construct a new operation instance.  This constructor
    * is used by the DefaultState implementation class as part of it's operation 
    * factory service.
    *
    * @param handler the uri identifying the operation handler
    */ 
    protected Operation( URI handler )
    {
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        m_handler = handler;
    }

   /**
    * Return the handler uri identifier associated with this operation.
    * @return the uri identiifying the handler
    */
    public URI getHandlerURI()
    {
        return m_handler;
    }
}
