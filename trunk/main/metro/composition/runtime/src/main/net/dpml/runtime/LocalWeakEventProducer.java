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

package net.dpml.metro.runtime;

import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.util.ExceptionHelper;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 */
abstract class LocalWeakEventProducer 
{
    private Map m_listeners = new WeakHashMap();

   /**
    * Internal synchronization lock.
    */
    protected final Object m_lock = new Object();

   /**
    * Abstract operation to be implemented by classes extending this base class.
    * An implementation is reposible for the posting of the event to associated 
    * listeners.  Event posting will be executed under a separate thread to the 
    * thread that initiated the event post.
    *
    * @param event the event to process
    */
    protected void processEvent( EventObject event )
    {
        final String error = 
          "Event class not recognized: " + event.getClass().getName();
        throw new IllegalArgumentException( error );
    }


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
        startEventDispatchThread();
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
    private static class EventDispatchThread extends Thread 
    {
        public void run() 
        {
            while( true ) 
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
                        Object object = EVENT_QUEUE.remove(0);
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
                    catch (InterruptedException e)
                    {
                        return;
                    }
                }

                Object source = event.getSource();
                if( source instanceof LocalWeakEventProducer )
                {
                    LocalWeakEventProducer producer = (LocalWeakEventProducer) source;
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
                        String msg = ExceptionHelper.packException( error, e, true );
                        System.err.println( msg );
                    }
                }
                else
                {
                    final String error = 
                      "Event source is not an instance of " + LocalWeakEventProducer.class.getName();
                    throw new IllegalStateException( error );
                }
            }
        }
    }

    private static Thread EVENT_DISPATCH_THREAD = null;

    /**
     * This method starts the event dispatch thread the first time it
     * is called.  The event dispatch thread will be started only
     * if someone registers a listener.
     */
    private static synchronized void startEventDispatchThread() 
    {
        if( EVENT_DISPATCH_THREAD == null ) 
        {
            EVENT_DISPATCH_THREAD = new EventDispatchThread();
            EVENT_DISPATCH_THREAD.setDaemon( true );
            EVENT_DISPATCH_THREAD.start();
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
            return (EventListener[])m_listeners.keySet().toArray( new EventListener[0] );
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
