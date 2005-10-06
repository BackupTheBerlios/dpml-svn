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

package net.dpml.state;

import java.util.EventObject;


/**
 * Event triggered as a result of a state change.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class StateEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final State m_from;
    private final State m_to;

   /**
    * Construct a new <code>StateEvent</code>.
    *
    * @param source the source compoent model
    * @param from the originating state
    * @param to the new current state
    */
    public StateEvent( final Object source, State from, State to )
    {
        super( source );
        m_from = from;
        m_to = to;
    }

    public State getFromState()
    {
        return m_from;
    }

    public State getToState()
    {
        return m_to;
    }

}

