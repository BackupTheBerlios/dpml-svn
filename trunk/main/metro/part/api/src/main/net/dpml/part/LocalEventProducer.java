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

package net.dpml.part;

import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.Logger;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class LocalEventProducer 
{
    //----------------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------------

   /**
    * Weak hashmap of event listeners.
    */
    private Map m_listeners = new WeakHashMap();
    
   /**
    * Logging channel.
    */
    private Logger m_logger;
    
   /**
    * Internal synchronization lock.
    */
    private final Object m_lock = new Object();
    
    //----------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------
    
   /**
    * Creation of a new local event producer.
    * @param logger the assigned logging channel
    */
    LocalEventProducer( Logger logger )
    {
        m_logger = logger;
    }
    
    //----------------------------------------------------------------------------
    // implementation
    //----------------------------------------------------------------------------
    
   /**
    * Return the internal context logging channel.
    * @return the logging channel
    */
    protected Logger getInternalLogger()
    {
        return m_logger;
    }
    
   /**
    * Return the synchronization lock.
    * @return the lock
    */
    protected Object getLock()
    {
        return m_lock;
    }
    
   /**
    * Dispose of the event producer.
    */
    void dispose()
    {
        m_logger.debug( "context event channel disposal" );
        synchronized( getLock() )
        {
            if( null != m_EVENT_DISPATCH_THREAD )
            {
                m_EVENT_DISPATCH_THREAD.dispose();
            }
        }
    }

   /**
    * Abstract operation to be implemented by classes extending this base class.
    * An implementation is reposible for the posting of the event to associated 
    * listeners.  Event posting will be executed under a separate thread to the 
    * thread that initiated the event post.
    *
    * @param event the event to process
    */
    protected abstract void processEvent( EventObject event );

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
            m_logger.debug( "adding context listener" );
            m_listeners.put( listener, null );
        }
        startEventDispatchThread( m_logger );
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
            m_logger.debug( "removing context listener" );
            m_listeners.remove( listener );
        }
    }

    /**
     * Queue of pending notification events.  When an event for which 
     * there are one or more listeners occurs, it is placed on this queue 
     * and the queue is notified.  A background thread waits on this queue 
     * and delivers the events.  This decouples event delivery from 
     * the application concern, greatly simplifying locking and reducing 
     * opportunity for deadlock.
     */
    private static final List EVENT_QUEUE = new LinkedList();

    /**
     * A single background thread ("the event notification thread") monitors
     * the event queue and delivers events that are placed on the queue.
     */
    /**
     * A single background thread ("the event notification thread") monitors
     * the event queue and delivers events that are placed on the queue.
     */
    private static class EventDispatchThread extends Thread 
    {
        private boolean m_continue = true;
        private final Logger m_logger;
        
        EventDispatchThread( Logger logger )
        {
            m_logger = logger;
        }
        
        void dispose()
        {
            synchronized( EVENT_QUEUE )
            {
                m_logger.debug( "terminating event dispatch thread" );
                m_continue = false;
                EVENT_QUEUE.notify();
            }
        }
        
        public void run()
        {
            while( m_continue ) 
            {
                // Wait on EVENT_QUEUE till an event is present
                EventObject event = null;
                synchronized( EVENT_QUEUE ) 
                {
                    try
                    {
                        while( EVENT_QUEUE.isEmpty() )
                        { 
                            EVENT_QUEUE.wait();
                        }
                        Object object = EVENT_QUEUE.remove( 0 );
                        try
                        {
                            event = (EventObject) object;
                        }
                        catch( ClassCastException cce )
                        {
                            final String error = 
                              "Unexpected class cast exception while processing an event." 
                              + "\nEvent: " + object;
                            throw new IllegalStateException( error );
                        }
                    }
                    catch( InterruptedException e )
                    {
                        return;
                    }
                }
                
                Object source = event.getSource();
                if( source instanceof LocalEventProducer )
                {
                    LocalEventProducer producer = (LocalEventProducer) source;
                    try
                    {
                        producer.processEvent( event );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unexpected error while processing event."
                          + "\nEvent: " + event
                          + "\nSource: " + this;
                        m_logger.warn( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Event source is not an instance of " 
                      + LocalEventProducer.class.getName();
                    throw new IllegalStateException( error );
                }
            }
        }
    }

    private static EventDispatchThread m_EVENT_DISPATCH_THREAD = null;

    /**
     * This method starts the event dispatch thread the first time it
     * is called.  The event dispatch thread will be started only
     * if someone registers a listener.
     */
    private static synchronized void startEventDispatchThread( Logger logger ) 
    {
        if( m_EVENT_DISPATCH_THREAD == null ) 
        {
            m_EVENT_DISPATCH_THREAD = new EventDispatchThread( logger );
            m_EVENT_DISPATCH_THREAD.setDaemon( true );
            m_EVENT_DISPATCH_THREAD.start();
        }
    }

    /**
     * Return this node's preference/node change listeners.  Even though
     * we're using a copy-on-write lists, we use synchronized accessors to
     * ensure information transmission from the writing thread to the
     * reading thread.
     */
    protected EventListener[] listeners() 
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
     */
    protected void enqueueEvent( EventObject event )
    {
        enqueueEvent( event, true );
    }

    /**
     * Enqueue an event for delivery to registered
     * listeners unless there are no registered
     * listeners.
     */
    protected void enqueueEvent( EventObject event, boolean asynchronouse )
    {
        if( m_listeners.size() != 0 )
        {
            if( asynchronouse )
            {    
                synchronized( EVENT_QUEUE ) 
                {
                    EVENT_QUEUE.add( event );
                    EVENT_QUEUE.notify();
                }
            }
            else
            {
                processEvent( event );
            }
        }
    }
}
