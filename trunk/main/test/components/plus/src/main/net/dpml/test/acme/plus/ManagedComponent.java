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

package net.dpml.test.acme.plus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import net.dpml.component.state.State;
import net.dpml.component.state.StateEvent;
import net.dpml.component.state.StateListener;

/**
 * Experimental component dealing with state management.
 * 
 * @author <a href="mailto:info@dpml.net">The Digital Product Meta Library</a>
 */
public class ManagedComponent implements StateListener
{
    // ------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------

    private final Logger m_logger;

    // ------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------

    /**
     * Creation of a component that describes an activity model.
     * 
     * @param logger the logging channel assigned by the container
     */
    public ManagedComponent( final Logger logger, Context context )
    {
        m_logger = logger;
        m_logger.info( "instantiated" );
        context.addStateListener( this );
    }

    /**
     * Notify the listener of a state change.  The supplied event
     * contains the originating state and the state that is about to 
     * be committed.
     *
     * @param event the state change event
     */
    public void stateChanged( final StateEvent event )
    {
        State source = event.getFromState();
        State destination = event.getToState();
        final String message = 
          "transitioning from '" 
          + source.getName() 
          + "' to '"
          + destination.getName()
          + "'";
        m_logger.info( message );
    }

    // ------------------------------------------------------------------
    // concerns
    // ------------------------------------------------------------------

    public interface Context
    {
         void addStateListener( StateListener listener );
    }

    // ------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------

    private Logger getLogger()
    {
        return m_logger;
    }

    //
    // The following methods are invoked by the state manager based on 
    // uris assigned to transitions.  The state manager locates the method
    // from the uri and invokes the operation after constructing arguments
    // from the possible set of Logger, Object (component impl), State (initial),
    // and State (target).  If the method requires the target state it must 
    // delcare both current and target state in the method parameters.
    //

    public void terminate( State state )
    {
        String name = state.getName();
        getLogger().info( "handling termination (current state: " + name + ")" );
    }

    public void start( State starting )
    {
        String name = starting.getName();
        getLogger().info( "handling start (current state: " + name + ")" );
    }

    public void stop()
    {
        getLogger().info( "handling stop" );
    }

    //
    // The following method is an example of a method invoked 
    // via an operation declaration. Operations do not trigger state 
    // change - instead they are gaurded methods that become visible
    // to a controlling application when the state to which the operation
    // is assigned becomes visible in the active state chain.
    //

    public void audit( State state )
    {
        StringBuffer buffer = new StringBuffer( "Audit Report" );
        buffer.append( "\n-------------------------------------------------" );
        buffer.append( "\nState Model:" );
        buffer.append( "\nCurrent State: " + state.getName() );
        buffer.append( "\n-------------------------------------------------" );
        buffer.append( state.list() );
        buffer.append( "\n-------------------------------------------------" );
        buffer.append( "\nClass Loader Stack" );
        buffer.append( "\n-------------------------------------------------" );
        buffer.append( "\n" + getClass().getClassLoader().toString() );
        buffer.append( "\n-------------------------------------------------" );
        getLogger().info( buffer.toString() );
    }

    // ------------------------------------------------------------------
    // static utilities
    // ------------------------------------------------------------------

    //
    // The following class is an example of a transition handler.  Instances
    // of this class may be registered with the state manager and referenced in 
    // transitiond via the urn "handler:[key]".
    //

    private static final Handler GENERIC_HANDLER = new Handler();

    public static class Handler
    {
        public void handle( ManagedComponent instance, State state, State target, Logger logger )
        {
            final String message = 
              "handling transition from [" 
              + state.getName()
              + "] to ["
              + target.getName()
              + "]";
            logger.info( message );
        }
    }
}
