/* 
 * Copyright 2004 Stephen J. McConnell.
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

import java.util.EventObject;
import java.util.EventListener;
import java.rmi.RemoteException;

import net.dpml.composition.event.WeakEventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.control.Component;
import net.dpml.part.control.AvailabilityEvent;
import net.dpml.part.control.AvailabilityListener;

import net.dpml.part.state.StateEvent;
import net.dpml.part.state.StateListener;
import net.dpml.part.state.State;

/**
 * A lifestyle handler provides support for the aquisition and release
 * of component instances.  An implementation is responsible for the  
 * handling of new instance creation based on lifestyle policy declared
 * in a component model.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: LifestyleManager.java 259 2004-10-30 07:24:40Z mcconnell $
 */
public abstract class AbstractHandler extends WeakEventProducer implements Component
{
    private static final State NULL_STATE = new State( true );

    private final Logger m_logger;

    private boolean m_initialized = false;

    public AbstractHandler( Logger logger )
    {
        super();
        m_logger = logger;
    }

   /**
    * Return an array of components providing services to this component.
    * @return the provider component array
    */
    public Component[] getProviders()
    {
        return new Component[0];
    }

   /**
    * Return the availability status of the model.
    * @return the availability status
    */
    public abstract boolean isOperational();

    public void addAvailabilityListener( AvailabilityListener listener )
    {
        super.addListener( listener );
    }

    public void removeAvailabilityListener( AvailabilityListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Return the current state of the component.
    * @return the current state
    */
    public State getState()
    {
        return NULL_STATE;
    }

    public void addStateListener( StateListener listener )
    {
        super.addListener( listener );
    }

    public void removeStateListener( StateListener listener )
    {
        super.removeListener( listener );
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof AvailabilityEvent )
        {
            AvailabilityEvent e = (AvailabilityEvent) event;
            EventListener[] listeners = listeners();
            for( int i=0; i<listeners.length; i++ )
            {
                EventListener listener = listeners[i];
                if( listener instanceof AvailabilityListener )
                {
                    AvailabilityListener availabilityListener = (AvailabilityListener) listener;
                    try
                    {
                        availabilityListener.availabilityChanged( e );
                    }
                    catch( Throwable t )
                    {
                        final String error =
                          "Listener raised an error duirng notification of a availability change.";
                        m_logger.warn( error, t );
                    }
                }
            }
        }
        else if( event instanceof StateEvent )
        {
            StateEvent e = (StateEvent) event;
            EventListener[] listeners = listeners();
            for( int i=0; i<listeners.length; i++ )
            {
                EventListener listener = listeners[i];
                if( listener instanceof StateListener )
                {
                    StateListener stateListener = (StateListener) listener;
                    try
                    {
                        stateListener.stateChanged( e );
                    }
                    catch( Throwable t )
                    {
                        final String error =
                          "Listener raised an error duirng notification of a state change.";
                        m_logger.warn( error, t );
                    }
                }
            }
        }
        else
        {
            final String error =
              "Event type not supported."
              + "\nEvent Class: " + event.getClass().getName();
            m_logger.warn( error );
        }
    }

    public abstract Manager getManager();

   /**
    * Return an initialized instance of the component.
    * @return the resolved instance
    */
    public Object resolve() throws Exception
    {
        return getManager().resolve( this, true );
    }

   /**
    * Return an initialized instance of the component using a supplied isolation policy.
    * If the isolation policy is TRUE an implementation shall make best efforts to isolate
    * implementation concerns under the object that is returned.  Typically isolation 
    * involves the creation of a proxy of a component implementation instance that 
    * exposes a component's service interfaces to a client.  If the isolation policy if
    * FALSE the implementation shall return the component implementation instance.
    * 
    * @param policy the isolation policy
    * @return the resolved instance
    */
    public Object resolve( boolean policy ) throws Exception
    {
        return getManager().resolve( this, policy );
    }

   /**
    * Initialize the component.  
    */
    //public void initialize() throws Exception
    //{
    //    getManager().initialize( this );
    //}

   /**
    * Applies a state transition identified by a supplied transition key.
    *
    * @param key the key identifying the transition to apply to the component's controller
    * @return the state resulting from the transition
    * @exception if a transition error occurs
    */
    public State apply( String key ) throws Exception
    {
        return getManager().apply( this, key );
    }

   /**
    * Executes an operation identified by a supplied operation key.
    *
    * @param key the key identifying the operation to execute 
    * @exception if a transition error occurs
    */
    public void execute( String key ) throws Exception
    {
        getManager().execute( this, key );
    }

   /**
    * Release a reference to an object managed by the instance.
    * 
    * @param instance the instance to release
    */
    public void release( Object instance )
    {
    }

   /**
    * Termination of the component.
    */
    //public void terminate()
    //{
    //    getManager().terminate( this );
    //}

    //protected void setInitialized( boolean flag )
    //{
    //    m_initialized = flag;
    //    AvailabilityEvent event = new AvailabilityEvent( this, flag );
    //    super.enqueueEvent( event );
    //}

    protected Logger getLogger()
    {
        return m_logger;
    }
}
