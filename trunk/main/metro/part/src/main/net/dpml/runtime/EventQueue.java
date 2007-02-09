/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.runtime;

import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;

import net.dpml.util.Logger;
import dpml.util.DefaultLogger;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in classes extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class EventQueue
{
    /**
     * Interface implemented by objects that maintain a collection of event listeners
     * and support for operational event propergation.
     *
     * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
     * @version @PROJECT-VERSION@
     */
    public interface EventHandler
    {
       /**
        * Return the associated event listeners.
        * @return the event listeners
        */
        EventListener[] getEventListeners();
        
       /**
        * Process the supplied event.
        * @param event the event to be processed
        */
        void processEvent( EventObject event );
    }

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------
    
    private final EventDispatchThread m_thread;
    
    private final Logger m_logger;
    
    private final List<EventObject> m_queue = new LinkedList<EventObject>();
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new event queue.
    * @param category the name used to construct a logging channel
    * @deprecated Use new EventQueue( logger, "Thread name" ) instead.
    */
    public EventQueue( String category ) 
    {
        this( category, "Event Dispatch Thread" );
    }

   /**
    * Creation of a new model.
    * @param logger the assigned logging channel
    * @exception NullPointerException if the supplied logging channel is null
    * @deprecated Use new EventQueue( logger, "Thread name" ) instead.
    */
    public EventQueue( Logger logger ) 
      throws NullPointerException
    {
        this( logger, "Event Dispatch Thread" );
    }
    
   /**
    * Creation of a new event queue.
    * @param category the name used to construct a logging channel
    * @param name the name to assign to the thread
    */
    public EventQueue( String category, String name ) 
    {
        this( getLoggerForCategory( category ), name );
    }

   /**
    * Creation of a new model.
    * @param logger the assigned logging channel
    * @param name the name to assign to the thread
    * @exception NullPointerException if the supplied logging channel or 
    *   thread name is null
    */
    public EventQueue( Logger logger, String name ) 
      throws NullPointerException
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_logger = logger;
        m_thread = new EventDispatchThread();
        m_thread.setName( name );
        m_thread.setDaemon( true );
        m_thread.start();
    }

    // ------------------------------------------------------------------------
    // EventQueue
    // ------------------------------------------------------------------------

   /**
    * Terminate the dispatch thread.
    */
    public synchronized void terminateDispatchThread()
    {
        if( null != m_thread )
        {
            m_thread.dispose();
        }
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }
    
    /**
     * A single background thread ("the event notification thread") monitors
     * the event queue and delivers events that are placed on the queue.
     */
    private class EventDispatchThread extends Thread 
    {
        private boolean m_continue = true;

        void dispose()
        {
            synchronized( m_queue )
            {
                m_continue = false;
                m_queue.notify();
            }
        }
        
        public void run() 
        {
            while( m_continue ) 
            {
                // Wait on m_queue till an event is present
                EventObject event = null;
                synchronized( m_queue ) 
                {
                    try 
                    {
                        while( m_continue && m_queue.isEmpty() )
                        { 
                            m_queue.wait();
                        }
                        if ( !m_continue )
                        {
                            break;
                        }
                        Object object = m_queue.remove( 0 );
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
                if( source instanceof EventHandler )
                {
                    EventHandler handler = (EventHandler) source;
                    try
                    {
                        handler.processEvent( event );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unexpected error while processing event."
                          + "\nEvent: " + event
                          + "\nSource: " + this;
                        getLogger().error( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Event source is not an instance of " 
                      + EventHandler.class.getName();
                    getLogger().error( error );
                }
            }
        }
    }

    /**
     * Enqueue an event for delivery to registered
     * listeners unless there are no registered
     * listeners.
     *
     * @param event the event object to add to the queue
     */
    public void enqueueEvent( EventObject event )
    {
        enqueueEvent( event, false );
    }

    /**
     * Enqueue an event for delivery to registered
     * listeners unless there are no registered
     * listeners.
     *
     * @param event the event object to add to the queue
     * @param waitForCompletion if TRUE the implementation will apply
     *   the event to the event source event handler and return on 
     *   copmpletion of evetn delivery
     */
    public void enqueueEvent( EventObject event, boolean waitForCompletion )
    {
        if( !waitForCompletion )
        {    
            synchronized( m_queue ) 
            {
                m_queue.add( event );
                m_queue.notify();
            }
        }
        else
        {
            Object source = event.getSource();
            if( source instanceof EventHandler )
            {
                EventHandler handler = (EventHandler) source;
                try
                {
                    handler.processEvent( event );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Unexpected error while processing event."
                      + "\nEvent: " + event
                      + "\nSource: " + source;
                    getLogger().error( error );
                }
            }
            else
            {
                final String error = 
                  "Event source is not an instance of " 
                  + EventHandler.class.getName()
                  + "\nSource: " + source.getClass().getName();
                throw new IllegalStateException( error );
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
            return new DefaultLogger( "" );
        }
        else
        {
            return new DefaultLogger( name );
        }
    }
}
