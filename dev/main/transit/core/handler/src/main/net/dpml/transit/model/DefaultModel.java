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

package net.dpml.transit.model;

import java.io.Serializable;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.adapter.LoggingAdapter;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 */
public abstract class DefaultModel extends UnicastRemoteObject
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * Internal logging channel.
    */
    private final Logger m_logger;

    private Map m_listeners = new WeakHashMap();

   /**
    * Internal synchronization lock.
    */
    protected final Object m_lock = new Object();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultModel( String name ) 
      throws NullPointerException, RemoteException
    {
        this( getLoggerForCategory( name ) );
    }

    public DefaultModel( Logger logger ) 
      throws NullPointerException, RemoteException
    {
        super();

        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        m_logger = logger;
    }

    // ------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------

    public Logger getLogger()
    {
        return m_logger;
    }

    // ------------------------------------------------------------------------
    // DefaultModel
    // ------------------------------------------------------------------------

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
        Logger logger = getLogger();
        startEventDispatchThread( logger );
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
        private Logger m_logger;

        EventDispatchThread( Logger logger )
        {
            m_logger = logger;
        }

        private Logger getLogger()
        {
            return m_logger;
        }

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
                if( source instanceof DefaultModel )
                {
                    DefaultModel producer = (DefaultModel) source;
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
                        getLogger().error( error );
                    }
                }
                else
                {
                    final String error = 
                      "Event source is not an instance of " 
                      + DefaultModel.class.getName();
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
    private static synchronized void startEventDispatchThread( Logger logger ) 
    {
        if( EVENT_DISPATCH_THREAD == null ) 
        {
            EVENT_DISPATCH_THREAD = new EventDispatchThread( logger );
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

    static Logger getLoggerForCategory( String name )
    {
        if( null == name )
        {
            return new LoggingAdapter( "" );
        }
        else
        {
            return new LoggingAdapter( name );
        }
    }
}
