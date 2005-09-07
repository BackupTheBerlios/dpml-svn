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

import java.io.Serializable;
import java.net.URI;

/**
 * The DefualtTransition class associated a transition handler and a target 
 * state.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class Transition implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final State m_target;
    private final URI m_handler;

   /**
    * Internal utility to construct a new transition instance.  This constructor
    * is used by the State state implementation class as part of it's transition 
    * factory operations.
    *
    * @param handler the uri identifying the handler
    * @param target the transition target state
    */ 
    protected Transition( URI handler, State target )
    {
        if( null == target )
        {
            throw new NullPointerException( "target" );
        }
        m_handler = handler;
        m_target = target;
    }

   /**
    * Return the handler uri identifier associated with this transition.
    * @return the transition handler identfier
    */
    public URI getHandlerURI()
    {
        return m_handler;
    }

   /**
    * Return the target of this transition.
    * @return the transition target
    */
    public State getTargetState()
    {
        return getTransitionTarget();
    }

   /**
    * Return the target of this transition.
    * @return the transition target
    */
    public State getTransitionTarget()
    {
        return m_target;
    }

    public String toString()
    {
        if( null == m_handler )
        {
            return "[transition target=" + m_target.getName() + "]";
        }
        else
        {
            return "[transition " + m_handler + " target=" + m_target.getName() + "]";
        }
    }
}
