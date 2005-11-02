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

package net.dpml.station.impl;

import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import net.dpml.transit.Logger;
import net.dpml.transit.util.ExceptionHelper;


/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 */
public abstract class EventProducer extends UnicastRemoteObject
{
   /**
    * Registered event listeners.
    */
    private EventListener[] m_listeners = new EventListener[0];

    private Logger m_logger;

   /**
    * Internal synchronization lock.
    */
    private final Object m_lock = new Object();

   /**
    * Creation of a new event producer.
    * @param logger the logging channel
    * @exception RemoteException if a remote exception occurs
    */
    public EventProducer( Logger logger ) throws RemoteException
    {
        super();

        m_logger = logger;
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
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
            Object[] old = m_listeners;
            m_listeners = new EventListener[ old.length + 1 ];
            System.arraycopy( old, 0, m_listeners, 0, old.length );
            m_listeners[old.length] = listener;
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
            if( m_listeners.length == 0 )
            {
                throw new IllegalArgumentException( "Listener not registered." );
            }
            // create the copy
            EventListener[] replacement = new EventListener[ m_listeners.length - 1 ];
            // copy listeners from 0 up to the listener being removed
            int i=0;
            while( i < replacement.length && m_listeners[i] != listener )
            {
                replacement[i] = m_listeners[i++];
            }
            // check that the listener has been located
            if( i == replacement.length &&  m_listeners[i] != listener )
            {
                throw new IllegalArgumentException( "Listener not registered." );
            }
            // complete the copy operation
            while( i < replacement.length )
            {
                replacement[i] = m_listeners[++i];
            }
            // commit the copy
            m_listeners = replacement;
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
                if( source instanceof EventProducer )
                {
                    EventProducer producer = (EventProducer) source;
                    try
                    {
                        producer.processEvent( event );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unexpected error while processing event."
                          + "\nEvent: " + event
                          + "\nSource: " + source;
                        String msg = ExceptionHelper.packException( error, e, true );
                        System.err.println( msg );
                    }
                }
                else
                {
                    final String error = 
                      "Event source is not an instance of " + EventProducer.class.getName();
                    throw new IllegalStateException( error );
                }
            }
        }
    }

    private static Thread m_EVENT_DISPATCH_THREAD = null;

   /**
    * This method starts the event dispatch thread the first time it
    * is called.  The event dispatch thread will be started only
    * if someone registers a listener.
    */
    private static synchronized void startEventDispatchThread() 
    {
        if( m_EVENT_DISPATCH_THREAD == null ) 
        {
            m_EVENT_DISPATCH_THREAD = new EventDispatchThread();
            m_EVENT_DISPATCH_THREAD.setDaemon( true );
            m_EVENT_DISPATCH_THREAD.start();
        }
    }

   /**
    * Return this node's preference/node change listeners.  Even though
    * we're using a copy-on-write lists, we use synchronized accessors to
    * ensure information transmission from the writing thread to the
    * reading thread.
    * @return the event listener array
    */
    protected EventListener[] listeners() 
    {
        synchronized( m_lock ) 
        {
            return m_listeners;
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
        if( m_listeners.length != 0 ) 
        {
            synchronized( EVENT_QUEUE ) 
            {
                EVENT_QUEUE.add( event );
                EVENT_QUEUE.notify();
            }
        }
    }
}
