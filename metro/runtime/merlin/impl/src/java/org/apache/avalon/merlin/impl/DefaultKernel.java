/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.merlin.impl;

import java.util.LinkedList;
import java.util.Iterator;

import org.apache.avalon.merlin.Kernel;
import org.apache.avalon.merlin.KernelContext;
import org.apache.avalon.merlin.KernelError;
import org.apache.avalon.merlin.KernelException;
import org.apache.avalon.merlin.KernelRuntimeException;
import org.apache.avalon.merlin.event.KernelEventListener;
import org.apache.avalon.merlin.event.KernelStateEvent;

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.Logger;

/**
 * Implementation of the default Merlin Kernel.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultKernel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultKernel implements Kernel, Disposable
{
    static private final String[] STATE_NAMES = 
    {
        "state: initializing",
        "state: initialized",
        "state: starting",
        "state: assembly",
        "state: deployment",
        "state: started",
        "state: stopping",
        "state: decommissioning",
        "state: dissassembly",
        "state: stopped"
    };
        

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final ContainmentModel m_model;

    private final LinkedList m_listeners = new LinkedList();

    private final KernelContext m_context;

    private final State m_state;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new Merlin Kernel.
    * @param context the creation context
    * @exception KernelException if a kernel initialization error occurs
    */
    public DefaultKernel( KernelContext context ) throws KernelException
    {
        if( context == null ) 
          throw new NullPointerException( "context" );

        m_context = context;
        m_state = new State( this );

        setState( INITIALIZING );

        try
        {
            m_model = context.getApplicationModel();
        }
        catch( Throwable e )
        {
            final String error = 
              "Cannot create application runtime.";
            throw new KernelError( error, e );
        }

        if( getLogger().isDebugEnabled() )
        {
            m_context.getLogger().debug( "kernel established" );
        }
        setState( INITIALIZED );
    }

    //--------------------------------------------------------------
    // Kernel
    //--------------------------------------------------------------

   /**
    * Return the current state of the kernel.
    * @return the kernel state
    */
    public int getState()
    {
        return m_state.getState();
    }

   /**
    * Add a kernel listener.
    * @param listener the kernel listener to be added
    */
    public void addKernelEventListener( KernelEventListener listener )
    {
        m_state.addKernelEventListener( listener );
    }

   /**
    * Remove a kernel listener.
    * @param listener the kernel listener to be removed
    */
    public void removeKernelEventListener( KernelEventListener listener )
    {
        m_state.removeKernelEventListener( listener );
    }

   /**
    * Return a model matching the supplied path.
    * @return the model
    */
    public DeploymentModel locate( String path )
    {
        return m_model.getModel( path );
    }

   /**
    * Return the root application block.
    * @return the application containment block
    */
    public ContainmentModel getModel()
    {
        return m_model;
    }

    //--------------------------------------------------------------
    // DefaultKernel
    //--------------------------------------------------------------

   /**
    * Initiate the establishment of the root container.
    * @exception Exception if a startup error occurs
    */
    public void startup() throws Exception
    {
        //
        // instantiate the runtime root application block
        //

        synchronized( m_state )
        {
            if( !isStartable() ) return;
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "application assembly" );
            }

            try
            {
                setState( ASSEMBLY );
                m_model.assemble();
            }
            catch( Exception e )
            {
                setState( INITIALIZED );
                final String error =
                  "Cannot assemble application due to exception.";
                throw new KernelException( error, e );
            }
            catch( Throwable e )
            {
                setState( INITIALIZED );
                final String error =
                  "Cannot assemble application due to throwable.";
                throw new KernelRuntimeException( error, e );
            }

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "application deployment" );
            }

            try
            {
                setState( DEPLOYMENT );
                m_model.commission();
            }
            catch( Exception e )
            {
                setState( INITIALIZED );
                final String error =
                  "Cannot deploy application.";
                throw new KernelException( error, e );
            }
            catch( Throwable e )
            {
                setState( INITIALIZED );
                final String error =
                  "Cannot deploy application.";
                throw new KernelRuntimeException( error, e );
            }
            
            setState( STARTED );
        }
    }

   /**
    * Shutdown the kernel during which orderly shutdown of all
    * installed blocks is undertaken.
    */
    public void shutdown()
    {
        synchronized( m_state )
        {
            if( !isStoppable() ) return;

            setState( STOPPING );

            try
            {
                setState( DECOMMISSIONING );
                m_model.decommission();
            }
            catch( Throwable e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    final String error =
                      "Ignoring block decommissioning error.";
                    getLogger().warn( error, e );
                }
            }

            /*
            try
            {
                setState( DISSASSEMBLY );
                getLogger().info( "dissassembly phase" );
                m_model.disassemble();
            }
            catch( Throwable e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    final String error =
                      "Ignoring application dissassembly error.";
                    getLogger().warn( error, e );
                }
            }
            */
            setState( STOPPED );
        }
    }

    //--------------------------------------------------------------
    // internal
    //--------------------------------------------------------------

    private boolean isStartable()
    {
        synchronized( m_state )
        {
            int state = m_state.getState();
            if( state == INITIALIZED ) return true;
            if( state == STOPPED ) return true;
            return false;
        }
    }

    private boolean isStoppable()
    {
        synchronized( m_state )
        {
            int state = m_state.getState();
            if( state == STARTED ) return true;
            return false;
        }
    }

    /**
     * Set the state of the kernel.  The method also triggers the 
     * emmission of a attribute change notification containing the 
     * old and new state value.
     *
     * @param state a string representing the new kernel state
     */
     private void setState( int state )
     {
         m_state.setState( state );
     }

    private Logger getLogger()
    {
        return m_context.getLogger();
    }


    private class State implements Runnable, Disposable
    {
        private int m_state = INITIALIZING;

        private LinkedList m_listeners = new LinkedList();

        private final Kernel m_kernel;

        private final SimpleFIFO m_events = new SimpleFIFO();

        private Thread m_notification;

        State( Kernel kernel )
        {
            m_kernel = kernel;
            m_notification = new Thread( this );
            m_notification.start();
        }

        public void addKernelEventListener( KernelEventListener listener )
        {
            synchronized( m_listeners )
            {
                m_listeners.add( listener );
            }
        }

        public void removeKernelEventListener( KernelEventListener listener )
        {
            synchronized( m_listeners )
            {
                m_listeners.remove( listener );
            }
        }

        public int getState()
        {
            return m_state;
        }

        public synchronized void setState( int state )
        {
            int oldValue = m_state;
            int newValue = state;

            m_state = newValue;

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( this.toString() );
            }

            KernelStateEvent event = 
              new KernelStateEvent( m_kernel, oldValue, newValue );
            m_events.put( event );
        }

        public void run()
        {
            try
            {
                while( true )
                {
                    KernelStateEvent event = (KernelStateEvent) m_events.get();
                    fireStateChangedEvent( event );
                }
            }
            catch( InterruptedException e )
            {
                // trigger by disposal
            }
            m_notification = null;
        }

        private void fireStateChangedEvent( final KernelStateEvent event )
        {
            Iterator iterator = m_listeners.iterator();
            while( iterator.hasNext() )
            {
                final KernelEventListener listener = 
                  (KernelEventListener) iterator.next();
                try
                {
                    listener.stateChanged( event );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Kernel listener raised an exception.";
                    getLogger().warn( error, e );
                }
            }
        }

        public void dispose()
        {
            if( null != m_notification )
            { 
                m_notification.interrupt();
            }
        }

        public String toString()
        {
            int s = m_state;
            if( s < STATE_NAMES.length )
            {
                return STATE_NAMES[ s ];
            }
            else
            {
                return "state: " + s;
            }
        }
    }

    public void dispose()
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "disposal" );
        }

        shutdown();

        m_state.dispose();
    }
}
