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

package net.dpml.part.state;


/**
 * Exception thrown when a request is made to apply a transition that is unknown
 * relative to the target state.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class NoSuchStateException extends RuntimeException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private String m_transition;

    /**
     * Construct a new <code>NoSuchStateException</code> instance.
     *
     * @param transition the transition name
     */
    public NoSuchStateException( final String transition )
    {
        super( transition );
    }

}

