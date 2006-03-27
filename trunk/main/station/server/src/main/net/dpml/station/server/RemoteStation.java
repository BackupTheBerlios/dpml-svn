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

package net.dpml.station.server; 

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.URL;
import java.util.Map;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.EventObject;

import net.dpml.station.Application;
import net.dpml.station.Callback;
import net.dpml.station.Manager;
import net.dpml.station.Station;
import net.dpml.station.StationException;

import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.StartupPolicy;
import net.dpml.station.ApplicationRegistry;

import net.dpml.lang.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.Disposable;

import net.dpml.lang.UnknownKeyException;

/**
 * The RemoteStation is responsible for the establishment of 
 * callback monitors to external processes established by the 
 * station manager.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RemoteStation extends UnicastRemoteObject implements Station, Manager
{
    private final RemoteApplicationRegistry m_registry;
    private final Map m_applications = new Hashtable();
    private final Logger m_logger;
    private final int m_port;
    private final Registry m_rmiRegistry;
    private final URL m_store;
    private final TransitModel m_model;
    private final LoggingServer m_server;
    private final Thread m_thread;
    
    private boolean m_terminated = false;
    
   /**
    * Creation of a station instance.
    *
    * @param logger the assigned logging channel
    * @param model the transit model
    * @param port the station port 
    * @param registryStorageUrl uri defining the registry backing store
    * @exception Exception if a exception occurs during establishment
    */
    public RemoteStation( 
      Logger logger, TransitModel model, int port, URL registryStorageUrl ) 
      throws Exception
    {
        super();
        
        m_logger = logger;
        m_port = port;
        m_store = registryStorageUrl;
        m_model = model;
        
        m_rmiRegistry = getLocalRegistry( port );
        
        try
        {
            m_rmiRegistry.bind( STATION_KEY, this );
        }
        catch( AlreadyBoundException e )
        {
            final String error =
             "An instance of the Station is already bound to port " + port;
            throw new StationException( error, e );
        }

        setShutdownHook( this );
        startEventDispatchThread();

        try
        {
            m_server = new LoggingServer( 2020 );
            m_thread = new Thread( m_server );
            m_thread.start();
        }
        catch( Exception e )
        {
            final String error =
             "Unexpected error while attempting to start the logging server on port " + 2020;
            throw new StationException( error, e );
        }
        
        if( getLogger().isDebugEnabled() )
        {
            if( null == registryStorageUrl )
            {
                getLogger().debug( "loading registry from default storage" );
            }
            else
            {
                getLogger().debug( "loading registry from [" + registryStorageUrl + "]" );
            }
        }
        
        m_registry = new RemoteApplicationRegistry( logger, registryStorageUrl );
        String[] keys = m_registry.getKeys();
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "registry established (" + keys.length + ")" );
        }
        
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            try
            {
                ApplicationDescriptor descriptor = 
                  m_registry.getApplicationDescriptor( key );
                if( StartupPolicy.AUTOMATIC.equals( descriptor.getStartupPolicy() ) )
                {
                    RemoteApplication application = getRemoteApplication( key );
                    application.start();
                }
            }
            catch( UnknownKeyException e )
            {
                throw new RuntimeException( e ); // will not happen
            }
        }
    }
    
   /**
    * Return a string containing info about the general setup of the station.
    * @return station configuration info
    */
    public String[] getInfo()
    {
        String[] values = new String[4];
        values[0] = "Port: " + m_port;
        values[1] = "Store: " + m_store;
        values[2] = "Basedir: " + System.getProperty( "user.dir" );
        values[3] = "Codebase: " 
          + getClass().getProtectionDomain().getCodeSource().getLocation();
        return values;
    }
    
   /**
    * Return an callback handler for the supplied id.
    * @param id the callback id
    * @return the callback handler
    * @exception UnknownKeyException if the id is unknown
    * @exception RemoteException if a remote error occurs
    */
    public Callback getCallback( String id ) throws UnknownKeyException, RemoteException
    {
        // TODO: improve this so that this is only called once per appliation
        return getRemoteApplication( id );
    }
    
   /**
    * Shutdown the station.
    */
    public void shutdown()
    {
        shutdown( true );
    }
    
   /**
    * Shutdown the station.
    * @param exit if true launch a process termination
    */
    private void shutdown( boolean exit )
    {
        synchronized( m_applications )
        {
            if( m_terminated )
            {
                return;
            }
            else
            {
                m_terminated = true;
            }
            
            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "initiating station shutdown" );
            }
            
            try
            {
                m_rmiRegistry.unbind( STATION_KEY );
            }
            catch( Exception e )
            {
                // ignore
            }
            try
            {
                RemoteApplication[] applications = getRemoteApplications();
                for( int i=0; i<applications.length; i++ )
                {
                    RemoteApplication application = applications[i];
                    application.shutdown();
                    UnicastRemoteObject.unexportObject( application, true );
                }
                UnicastRemoteObject.unexportObject( m_registry, true );
            }
            catch( Exception e )
            {
                // ignore
            }
            try
            {
                int n =  m_server.getErrorCount();
                if( n > 0 )
                {
                    getLogger().warn( "logging issues: " + n );
                }
                m_thread.interrupt();
            }
            catch( Exception e )
            {
                // ignore
            }
            
            finally
            {
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "station shutdown complete" );
                }
                
                if( exit )
                {
                    if( m_model instanceof Disposable )
                    {
                        try
                        {
                            Disposable disposable = (Disposable) m_model;
                            disposable.dispose();
                        }
                        catch( Exception e )
                        {
                            // ignore
                        }
                    }
                    
                    if( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "terminating process" );
                    }
                    
                    Thread thread = new Thread(
                      new Runnable()
                      {
                        public void run()
                        {
                            RemoteStation.m_DISPATCH.dispose();
                            System.exit( 0 );
                        }
                      }
                    );
                    thread.start();
                }
            }
        }
    }
    
   /**
    * Return the application registry.
    * @return the registry
    */
    public ApplicationRegistry getApplicationRegistry()
    {
        return m_registry;
    }

   /**
    * Return an application reference for the supplied key.
    * @param key the application key
    * @return the application
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    public Application getApplication( String key ) throws UnknownKeyException, RemoteException
    {
        return getRemoteApplication( key );
    }
    
   /**
    * Process a log record.  The implementation
    * prepends the log record message with the process id.  Subsequent processing
    * of log records is subject to the logging configuration applied to the 
    * JVM process in which the server is established.
    *
    * @param process the process id of the jvm initiating the log record
    * @param record the log record
    * @exception RemoteException is a remote exception occurs
    */
    //public void log( PID process, LogRecord record ) throws RemoteException
    //{
    //    m_loggingServer.log( process, record );
    //}
    
    
   /**
    * Return an application reference for the supplied key.
    * @param key the application key
    * @return the application
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    RemoteApplication getRemoteApplication( String key ) throws UnknownKeyException, RemoteException
    {
        synchronized( m_applications )
        {
            if( m_applications.containsKey( key ) )
            {
                return (RemoteApplication) m_applications.get( key );
            }
            else
            {
                Logger logger = getLogger().getChildLogger( key );
                ApplicationDescriptor descriptor = m_registry.getApplicationDescriptor( key );
                RemoteApplication application = 
                  new RemoteApplication( logger, descriptor, key, m_port );
                m_applications.put( key, application );
                return application;
            }
        }
    }

   /**
    * Return an array of all remote applications.
    * @return the applications array
    */
    RemoteApplication[] getRemoteApplications()
    {
        synchronized( m_applications )
        {
            return (RemoteApplication[]) m_applications.values().toArray( new RemoteApplication[0] );
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private Registry getLocalRegistry( int port ) throws RemoteException
    {
        try
        {
            Registry registry = LocateRegistry.createRegistry( port );
            getLogger().debug( "created local registry on port " + port );
            return registry;
        }
        catch( RemoteException e )
        {
            Registry registry = LocateRegistry.getRegistry( port );
            getLogger().debug( "using local registry on port " + port );
            return registry;
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
    * Enqueue an event for delivery to registered
    * listeners unless there are no registered
    * listeners.
    * @param event the event to enqueue
    */
    static void enqueueEvent( EventObject event )
    {
        synchronized( EVENT_QUEUE ) 
        {
            EVENT_QUEUE.add( event );
            EVENT_QUEUE.notify();
        }
    }
    
    /**
     * A single background thread ("the event notification thread") monitors
     * the event queue and delivers events that are placed on the queue.
     */
    private static class EventDispatchThread extends Thread 
    {
        private final Logger m_logger;
        
        private boolean m_continue = true;
        
        EventDispatchThread( Logger logger )
        {
            m_logger = logger;
            m_logger.debug( "starting event dispatch thread" );
        }
        
        void dispose()
        {
            synchronized( EVENT_QUEUE )
            {
                m_logger.debug( "stopping event dispatch thread" );
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
                if( source instanceof UnicastEventSource )
                {
                    UnicastEventSource producer = (UnicastEventSource) source;
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
                        m_logger.warn( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Event source [" 
                      + source.getClass().getName()
                      + "] is not an instance of " + UnicastEventSource.class.getName();
                    throw new IllegalStateException( error );
                }
            }
            
            m_logger.info( "Controller event queue terminating." );
        }
    }

    private static EventDispatchThread m_DISPATCH = null;

    /**
     * This method starts the event dispatch thread the first time it
     * is called.  The event dispatch thread will be started only
     * if someone registers a listener.
     */
    private synchronized void startEventDispatchThread()
    {
        if( m_DISPATCH == null )
        {
            Logger logger = getLogger();
            m_DISPATCH = new EventDispatchThread( logger );
            m_DISPATCH.setDaemon( true );
            m_DISPATCH.start();
        }
    }
        
   /**
    * Create a shutdown hook that will trigger shutdown of the supplied plugin.
    * @param station the station
    */
    public static void setShutdownHook( final RemoteStation station )
    {
        //
        // Create a shutdown hook to trigger clean disposal of the
        // controller
        //
        
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
              public void run()
              {
                  try
                  {
                      station.shutdown();
                  }
                  catch( Throwable e )
                  {
                      System.err.println( e.toString() );
                  }
                  System.runFinalization();
              }
          }
        );
    }
    
}
