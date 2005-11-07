
package net.dpml.station.impl; 

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ServerException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Logger;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.Connection;
import net.dpml.transit.util.CLIHelper;

import net.dpml.profile.info.StartupPolicy;
import net.dpml.profile.model.ApplicationProfile;
import net.dpml.profile.model.ApplicationRegistry;

import net.dpml.depot.GeneralException;
import net.dpml.depot.Handler;
import net.dpml.depot.LoggingService;

import net.dpml.station.Station;
import net.dpml.station.Application;

/**
 * The DepotStation class provides support for the establishment and maintenance
 * of DPML server processes.  Each process represents an identifiable virtual machine
 * and associated component model. 
 */
public class DepotStation extends UnicastRemoteObject implements Station, Handler
{
    private int m_count = 0;
    private final Logger m_logger;
    private Hashtable m_table = new Hashtable();
    private ApplicationRegistry m_model;
    private String[] m_args;

   /**
    * Creation of a station instance.
    *
    * @param logger the assigned logging channel
    * @param args supplimentary command line arguments
    * @exception Exception if a exception occurs during establishment
    */
    public DepotStation( Logger logger, String[] args ) throws Exception
    {
        super();

        m_logger = logger;
        m_args = args;

        try
        {
            Repository repository = Transit.getInstance().getRepository();
            ClassLoader classloader = DepotStation.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( classloader );
            URI uri = new URI( DEPOT_PROFILE_URI );
            m_model = (ApplicationRegistry) repository.getPlugin( 
              classloader, uri, new Object[]{logger} );

            //
            // startup the general registry
            //
            
            int port = getStationPort();
            Connection connection = new Connection( null, port, true, true );
            Registry registry = getRegistry( connection );
            
            try
            {
                registry.bind( STATION_KEY, this );
            }
            catch( AlreadyBoundException e )
            {
                final String error =
                  "An instance of the Station is already bound to port " + port;
                throw new GeneralException( error );
            }

            try
            {
                LoggingService logging = new LoggingServer();
                registry.bind( LoggingService.LOGGING_KEY, logging );
            }
            catch( AlreadyBoundException e )
            {
                final String error =
                  "An instance of the Logging Service is already bound to port " + port;
                throw new GeneralException( error );
            }

            ApplicationProfile[] profiles = m_model.getApplicationProfiles();
            for( int i=0; i<profiles.length; i++ )
            {
                ApplicationProfile profile = profiles[i];
                String key = profile.getID();
                logger.info( "profile: " + key );
                String urn = "registry:" + key;

                //
                // TODO:
                //
                // for the moment we are registring the application profile into 
                // into the registry so that the supbrocess picks up a reference to 
                // the same object we are managing - however, this leaves the profile
                // exposed - instead we need to setup a uri that references the station
                // instance together with a query fragment identifying the application
                // instance (e.g. registry:/dpml/station?profile=/dpml/planet/http) 
                // following which we can handle security policy controls that restrict 
                // the query operations based on assigned security policies
                //

                Application application = new DefaultApplication( logger, profile, urn );
                m_table.put( key, application );
                registry.rebind( key, profile );
                StartupPolicy policy = profile.getStartupPolicy();
                if( ApplicationProfile.AUTOMATIC == policy )
                {
                    try
                    {
                        application.start();
                    }
                    catch( Exception e )
                    {
                        final String error = 
                          "Startup error raised by application [" + key + "]";
                        getLogger().warn( error );
                    }
                }
            }

            String[] list = registry.list();
            logger.info( "registry: " + list.length );
            for( int i=0; i<list.length; i++ )
            {
                logger.info( "entry: " + list[i] );
            }
        }
        catch( RemoteException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error occured while establishing depot station.";
            throw new ServerException( error, e );
        }
    }

    private int getStationPort() throws GeneralException
    {
        if( CLIHelper.isOptionPresent( m_args, "-port" ) )
        {
            String value = CLIHelper.getOption( m_args, "-port" );
            m_args = CLIHelper.consolidate( m_args, "-port", 1 );
            try
            {
                return Integer.parseInt( value );
            }
            catch( NumberFormatException e )
            {
                final String error = 
                  "Supplied -port argument value [" 
                  + value 
                  + "] could not be converted to a number.";
                throw new GeneralException( error );
            }
        }
        else
        {
            return Registry.REGISTRY_PORT;
        }
    }

   /**
    * Terminate the station.
    */
    public void destroy()
    {
        String[] keys = (String[]) m_table.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            final String key = keys[i];
            Application application = (Application) m_table.get( key );
            try
            {
                application.stop();
            }
            catch( Throwable e )
            {
                getLogger().warn( e.toString() );
            }
        }
    }

   /**
    * Return the application registry.
    * @return the registry
    */
    public ApplicationRegistry getApplicationRegistry()  
    {
        return m_model;
    }

   /**
    * Add an application to the station.
    * @param profile the application profile
    * @return the application
    * @exception DuplicateKeyException if an application is already assigned to the key
    * @exception RemoteException if a remote error occurs
    */
    public Application addApplication( ApplicationProfile profile )
      throws DuplicateKeyException, RemoteException
    {
        throw new UnsupportedOperationException( "addApplication/1" );
    }

   /**
    * Remove an application from the station.
    * @param key the application key
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    public void removeApplication( String key ) throws UnknownKeyException, RemoteException
    {
        throw new UnsupportedOperationException( "removeApplication/1" );
    }

   /**
    * Return an array of application names managed by the station.
    * @return the application names
    * @exception RemoteException if a remote error occurs
    */
    public String[] getApplicationKeys() throws RemoteException
    {
        try
        {
            ApplicationProfile[] profiles = m_model.getApplicationProfiles();
            String[] keys = new String[ profiles.length ];
            for( int i=0; i < profiles.length; i++ )
            {
                ApplicationProfile profile = profiles[i];
                String key = profile.getID();
                keys[i] = key;
            }
            return keys;
        }
        catch( Exception e )
        {
            final String error = 
              "An unexpected error occured while resolving profile keys.";
            throw new ServerException( error, e );
        }
    }

   /**
    * Return the application profile.
    * @param key the profile key
    * @return the named profile
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    public ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException
    {
        return m_model.getApplicationProfile( key );
    }

   /**
    * Return a named application.
    * @param key the application key
    * @return the named aplication
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    public Application getApplication( String key ) throws UnknownKeyException, RemoteException
    {
        Application application = (Application) m_table.get( key );
        if( null == application )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return application;
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    Registry getRegistry( Connection connection ) throws RemoteException 
    {
        if( null == connection )
        {
            return null;
        }
        else
        {
            String host = connection.getHost();
            int port = connection.getPort();

            if( ( null == host ) || ( "localhost".equals( host ) ) )
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
            else
            {
                return LocateRegistry.getRegistry( host, port );
            }
        }
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";
}
