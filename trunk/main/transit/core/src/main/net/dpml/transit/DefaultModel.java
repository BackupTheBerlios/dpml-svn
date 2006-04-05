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

package net.dpml.transit;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.util.Logger;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in classes extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class DefaultModel extends UnicastRemoteObject implements Disposable
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
    private final Object m_lock = new Object();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new model.
    * @param name the name used to construct a logging channel
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultModel( String name ) 
      throws RemoteException
    {
        this( getLoggerForCategory( name ) );
    }

   /**
    * Creation of a new model.
    * @param logger the assigned logging channel
    * @exception NullPointerException if the supplied logging channel is null
    * @exception RemoteException if a remote exception occurs
    */
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
    // DefaultModel
    // ------------------------------------------------------------------------

   /**
    * Abstract method that must be implemented by classes extending this class.
    * An implementation is responsible for handling the processing of events 
    * it is aware of or throwing an llegalArgumentException in the case of 
    * unrecognized event types.  A typical implementation is shown in the following
    * code fragment:
    * 
    * <pre>
    * protected void processEvent( EventObject eventObject )
    * {
    *    if( eventObject instanceof ProxyEvent )
    *    {
    *        ProxyEvent event = (ProxyEvent) eventObject;
    *        processProxyEvent( event );
    *    }
    *    else
    *    {
    *        final String error = 
    *          "Event class not recognized: " + eventObject.getClass().getName();
    *        throw new IllegalArgumentException( error );
    *    }
    * }
    *
    * private void processProxyEvent( ProxyEvent event )
    * {
    *    EventListener[] listeners = super.listeners();
    *    for( int i=0; i&lt;listeners.length; i++ )
    *    {
    *        EventListener listener = listeners[i];
    *        if( listener instanceof ProxyListener )
    *        {
    *            ProxyListener pl = (ProxyListener) listener;
    *            try
    *            {
    *                pl.proxyChanged( event );
    *            }
    *            catch( Throwable e )
    *            {
    *                final String error =
    *                  "Proxy listener notification error.";
    *                getLogger().error( error, e );
    *            }
    *        }
    *    }
    * }
    * </pre>
    * 
    * @param event the event to process
    */
    protected abstract void processEvent( EventObject event );

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    Logger getLoggingChannel()
    {
        return getLogger();
    }
    
   /**
    * Dispose of the model.
    */
    public synchronized void dispose()
    {
        EventListener[] listeners = listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            removeListener( listener );
        }
        getLogger().debug( "disposed" );
    }
    
   /**
    * Terminate the dispatch thread.
    */
    synchronized void terminateDispatchThread()
    {
        if( null != m_EVENT_DISPATCH_THREAD )
        {
            m_EVENT_DISPATCH_THREAD.dispose();
        }
    }
    
   /**
    * Add a listener to the set of listeners handled by the model.
    * @param listener the event listener
    * @exception NullPointerException if the supplied listener is null
    */
    protected void addListener( EventListener listener ) throws NullPointerException 
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
    * @exception NullPointerException if the supplied listener is null
    */
    protected void removeListener( EventListener listener ) throws NullPointerException 
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
        private boolean m_continue = true;
        
        private Logger m_logger;
        
        EventDispatchThread( Logger logger )
        {
            m_logger = logger;
        }
        
        void dispose()
        {
            synchronized( EVENT_QUEUE )
            {
                m_continue = false;
                EVENT_QUEUE.notify();
            }
        }
        
        private Logger getLogger()
        {
            return m_logger;
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
                        while( m_continue && EVENT_QUEUE.isEmpty() )
                        { 
                            EVENT_QUEUE.wait();
                        }
                        if ( !m_continue )
                        {
                            break;
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
    * Return the internal synchronization lock object.
    * @return the lock object
    */
    protected Object getLock()
    {
        return m_lock;
    }

    /**
     * Return the set of registered listeners.
     * @return an array of registered listeners
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
     *
     * @param event the event object to add to the queue
     */
    protected void enqueueEvent( EventObject event )
    {
        enqueueEvent( event, true );
    }

    /**
     * Enqueue an event for delivery to registered
     * listeners unless there are no registered
     * listeners.
     *
     * @param event the event object to add to the queue
     * @param asynchronouse TRUE if asynchronouse delivery
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

   /**
    * Return a logging channel for the supplied name.
    * @param name the name to use in construction of the logging channel
    * @return the logging channel
    */
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
