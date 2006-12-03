/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package net.dpml.util;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NoSuchObjectException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.WeakHashMap;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class UnicastEventSource extends UnicastRemoteObject implements EventHandler
{
   /**
    * Internal synchronization lock.
    */
    private final Object m_lock = new Object();
    
   /**
    * The controller that provides the main event dispath thread.
    */
    private final EventQueue m_queue;
    
    private final WeakHashMap m_listeners = new WeakHashMap();
    
    private final Logger m_logger;

   /**
    * Creation of a new <tt>UnicastEventSource</tt>.
    * @param queue the event queue
    * @param logger the assigned logging channel
    * @exception RemoteException if a remote I/O exception occurs
    */
    protected UnicastEventSource( EventQueue queue, Logger logger ) throws RemoteException
    {
        super();
        m_queue = queue;
        m_logger = logger;
    }
    
    //--------------------------------------------------------------------------
    // internal
    //--------------------------------------------------------------------------
    
   /**
    * Return the logging channel assigned to the event source.
    * @return the logging channel
    */
    protected Logger getLocalLogger()
    {
        return m_logger;
    }
    
   /**
    * Return the event queue.
    * @return the queue
    */
    protected EventQueue getEventQueue()
    {
        return m_queue;
    }
    
   /**
    * Abstract operation to be implemented by classes extending this base class.
    * An implementation is reposible for the posting of the event to associated 
    * listeners.  Event posting will be executed under a separate thread to the 
    * thread that initiated the event post.
    *
    * @param event the event to process
    */
    public abstract void processEvent( EventObject event );

   /**
    * Add a listener to the set of listeners handled by this producer.
    * @param listener the event listener
    */
    protected void addListener( EventListener listener ) 
    {
        if( null == listener )
        {
            throw new NullPointerException( "listener" );
        }
        synchronized( m_lock ) 
        {
            m_listeners.put( listener, null );
        }
    }

   /**
    * Remove a listener to the set of listeners handled by this producer.
    * @param listener the event listener
    */
    protected void removeListener( EventListener listener )
    {
        if( null == listener )
        {
            throw new NullPointerException( "listener" );
        }
        synchronized( m_lock )
        {
            m_listeners.remove( listener );
        }
    }

    
   /**
    * Return the array of registered event listeners.
    *
    * @return the event listeners
    */
    public EventListener[] getEventListeners() 
    {
        synchronized( m_lock )
        {
            return (EventListener[]) m_listeners.keySet().toArray( new EventListener[0] );
        }
    }

   /**
    * Enqueue an event for delivery to registered
    * listeners unless there are no registered
    * listeners.
    * @param event the event to enqueue
    */
    protected void enqueueEvent( EventObject event )
    {
        if( m_listeners.size() > 0 )
        {
            m_queue.enqueueEvent( event );
        }
    }
    
   /**
    * Return the internal synchronization lock.
    * @return the lock object
    */
    protected Object getLock()
    {
        return m_lock;
    }
    
   /**
    * Terminate the event source.
    */
    public void terminate()
    {
        synchronized( m_lock )
        {
            EventListener[] listeners = getEventListeners();
            for( int i=0; i < listeners.length; i++ )
            {
                EventListener listener = listeners[i];
                removeListener( listener );
            }
        }
        
        Thread thread = new Terminator( this, m_logger );
        thread.start();
    }
    
   /**
    * Internal class that handles instance retraction for the RMI runtime.
    */
    private class Terminator extends Thread
    {
        private final UnicastEventSource m_source;
        private final Logger m_logger;
        
       /**
        * Internal class terminator that handles unexport of an event source
        * under a separate thread.
        * @param source the event source instance
        * @param logger the event source logger
        */
        Terminator( UnicastEventSource source, Logger logger )
        {
            m_source = source;
            m_logger = logger;
        }
        
       /**
        * Terminator execution.
        */
        public void run()
        {
            try
            {
                UnicastRemoteObject.unexportObject( m_source, true );
                m_logger.trace( "terminated " + m_source.getClass().getName() );
            }
            catch( NoSuchObjectException e )
            {
                boolean ignoreThis = true; // object has not been exported
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unexpected remote exception while retracting component handler remote reference.";
                m_logger.warn( error, e );
            }
        }
    }
}
