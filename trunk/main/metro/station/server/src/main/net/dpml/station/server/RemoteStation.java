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
import java.util.logging.LogRecord;

import net.dpml.station.Application;
import net.dpml.station.Callback;
import net.dpml.station.Manager;
import net.dpml.station.Station;
import net.dpml.station.StationException;

import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.StartupPolicy;
import net.dpml.station.ApplicationRegistry;

import net.dpml.transit.Logger;
import net.dpml.transit.LoggingService;
import net.dpml.transit.PID;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.model.UnknownKeyException;

/**
 * The RemoteStation is responsible for the establishment of 
 * callback monitors to external processes established by the 
 * station manager.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RemoteStation extends UnicastRemoteObject implements Station, Manager, LoggingService
{
    private final RemoteApplicationRegistry m_registry;
    private final Map m_applications = new Hashtable();
    private final Logger m_logger;
    private final int m_port;
    private final Registry m_rmiRegistry;
    private final LoggingServer m_loggingServer;
    private final URL m_store;
    
   /**
    * Creation of a station instance.
    *
    * @param logger the assigned logging channel
    * @param port the station port 
    * @param registryStorageUrl uri defining the registry backing store
    * @exception Exception if a exception occurs during establishment
    */
    public RemoteStation( Logger logger, int port, URL registryStorageUrl ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_port = port;
        m_store = registryStorageUrl;
        
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
        
        try
        {
            m_loggingServer = new LoggingServer();
            m_rmiRegistry.bind( LoggingService.LOGGING_KEY, m_loggingServer );
        }
        catch( AlreadyBoundException e )
        {
            final String error =
             "An instance of the logging service is already bound to port " + port;
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
        synchronized( m_applications )
        {
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
                m_rmiRegistry.unbind( LoggingService.LOGGING_KEY );
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
                }
                UnicastRemoteObject.unexportObject( m_registry, true );
            }
            catch( Exception e )
            {
                // ignore
            }
            finally
            {
                Thread thread = new Thread(
                  new Runnable()
                  {
                      public void run()
                      {
                        System.exit( 0 );
                      }
                  }
                );
                thread.start();
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
    public void log( PID process, LogRecord record ) throws RemoteException
    {
        m_loggingServer.log( process, record );
    }
    
    
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
                Logger logger = new LoggingAdapter( key );
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
}
