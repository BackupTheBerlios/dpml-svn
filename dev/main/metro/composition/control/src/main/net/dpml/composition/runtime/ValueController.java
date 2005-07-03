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

package net.dpml.composition.runtime;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.dpml.composition.control.CompositionController;

import net.dpml.part.manager.Component;
import net.dpml.part.state.NoSuchOperationException;
import net.dpml.part.state.NoSuchTransitionException;
import net.dpml.part.state.State;

/**
 * The ValueController class manages value instances.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ValueController implements Manager
{
    private URI m_uri;

    public ValueController( CompositionController controller )
    {
        super();

        URI uri = controller.getURI();
        String path = uri.getSchemeSpecificPart();
        m_uri = CompositionController.createURI( "manager", path );
    }

    public String getName()
    {
        return getClass().getName();
    }

    public URI getURI()
    {
        return m_uri;
    }

   /**
    * Initialize the component.  
    */
    public void initialize( Component entry ) throws Exception
    {
    }

   /**
    * Return the instance managed by this manager.
    * @return the managed instance
    */
    public Object resolve( Component entry, boolean policy  )
    {
        return ((ValueHandler)entry).getInstance();
    }

    public void release( Object instance )
    {
    }

   /**
    * Return the current state of the component.
    * @return the current state
    */
    public State getState( Component entry )
    {
        return AVAILABLE;
    }

   /**
    * Applies a state transition identified by a supplied transition key.
    *
    * @param key the key identifying the transition to apply to the component's controller
    * @return the state resulting from the transition
    * @exception if a transition error occurs
    */
    public State apply( Component entry, String key ) throws Exception
    {
        throw new NoSuchTransitionException( key );
    }

   /**
    * Executes an operation identified by a supplied operation key.
    *
    * @param key the key identifying the operation to execute 
    * @exception if a transition error occurs
    */
    public void execute( Component entry, String key ) throws Exception
    {
        throw new NoSuchOperationException ( key );
    }

   /**
    * Termination of the component.
    */
    public void terminate( Component entry )
    {
    }

    public static final State AVAILABLE = new State( true );
}
