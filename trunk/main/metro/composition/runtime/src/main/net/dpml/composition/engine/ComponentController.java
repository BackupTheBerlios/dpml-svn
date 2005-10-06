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

package net.dpml.composition.engine;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.EventObject;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.event.EventProducer;

import net.dpml.component.model.ComponentModel;

import net.dpml.logging.Logger;

import net.dpml.part.Context;
import net.dpml.part.Control;
import net.dpml.part.ControlException;
import net.dpml.part.Handler;
import net.dpml.part.HandlerException;

import net.dpml.state.StateMachine;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentController extends EventProducer implements Control
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final CompositionController m_controller;

    //--------------------------------------------------------------------------
    // contructor
    //--------------------------------------------------------------------------

    public ComponentController( Logger logger, CompositionController controller ) throws RemoteException
    {
        super();
        
        m_logger = logger;
        m_controller = controller;
    }

    //--------------------------------------------------------------------------
    // Control
    //--------------------------------------------------------------------------
    
   /**
    * Create a new runtime handler using a supplied context.
    * @param context the managed context
    * @return the runtime handler
    */
    public Handler createHandler( Context context )
    {
        if( context instanceof ComponentModel )
        {
            ComponentModel model = (ComponentModel) context;
            try
            {
                return new ComponentHandler( m_logger, this, model );
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Creation of a new component handler failed due to an remote exception.";
                throw new RuntimeException( error, e );
            }
        }
        else
        {
            final String error = 
              "Context class ["
              + context.getClass().getName()
              + "] is not supported.";
            throw new RuntimeException( error );
        }
    }

   /**
    * Initiate activation of a runtime handler.
    * @param handler the runtime handler
    * @exception Exception if an activation error occurs
    */
    public void activate( Handler handler ) throws ControlException
    {
        if( handler instanceof ComponentHandler )
        {
            ComponentHandler component = (ComponentHandler) handler;
            processActivation( component );
        }
        else
        {
            final String error = 
              "Runtime handler class ["
              + handler.getClass().getName()
              + "] is not recognized.";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Initiate deactivation of a supplied handler.
    * @exception Exception if an activation error occurs
    */
    public void deactivate( Handler handler )
    {
        if( handler instanceof ComponentHandler )
        {
            ComponentHandler component = (ComponentHandler) handler;
            processDeactivation( component );
        }
        else
        {
            final String error = 
              "Runtime handler class ["
              + handler.getClass().getName()
              + "] is not recognized.";
            throw new UnsupportedOperationException( error );
        }
    }

    //--------------------------------------------------------------------------
    // ComponentController
    //--------------------------------------------------------------------------
    
    public Object instantiate( ComponentHandler handler )
    {
        return null;
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
        // TODO: post creation, activation and deactivation events
    }
    
    //--------------------------------------------------------------------------
    // internals
    //--------------------------------------------------------------------------
    
    private void processActivation( ComponentHandler handler ) throws ControlException
    {
        getLogger().info( "activating: [" + handler + "]" );
        synchronized( handler )
        {
            // check with the handler for its activation status
            
            if( handler.isActive() )
            {
                return;
            }
            else
            {
                // build constructor arguments
                // instantiate the instance
            
                StateMachine machine = handler.getStateMachine();
                Object instance = handler.getInstance();
                boolean ok = true;
                try
                {
                    machine.initialize( instance );
                }
                catch( InvocationTargetException e )
                {
                    ok = false;
                    final String error = 
                      "Exception raised by invocation target.";
                    throw new ControlException( error, e );
                }
                finally
                {
                    if( !ok )
                    {
                        processDeactivation( handler );
                    }
                }
            }
        }
    }

    private void processDeactivation( ComponentHandler handler )
    {
        getLogger().info( "deactivating: [" + handler + "]" );
        synchronized( handler )
        {
            if( !handler.isActive() )
            {
                return;
            }
            else
            {
                // 1. get dependent handlers and notify them of non-availability
                // 2. terminate the handler
                
                StateMachine machine = handler.getStateMachine();
                Object instance = handler.getInstance();
                machine.terminate( instance );
                
                // 3. decommission the instance
            }
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
