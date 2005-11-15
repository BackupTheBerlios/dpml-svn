/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.part;

import java.net.URI;

/**
 * Exception thrown by a local handler that wraps an exception thrown 
 * by a foreign handler.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class DelegationException extends PartException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private URI m_target;

   /**
    * Creation of a new delegation exception.
    * @param target the uri of the target handler that raised the causal exception
    * @param message exception message
    * @param cause the causal exception
    */
    public DelegationException( URI target, String message, Throwable cause )
    {
        super( message, cause );
        m_target = target;
    }

   /**
    * Return the uri identifying the delegation target handler.
    * @return the URI of the delegate handler
    */
    public URI getDelegationTarget()
    {
        return m_target;
    }

}

