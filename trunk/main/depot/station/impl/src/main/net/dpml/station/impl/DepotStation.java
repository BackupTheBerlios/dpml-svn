
package net.dpml.station.impl; 

import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ServerException;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.activation.ActivationSystem;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupDesc;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.Activatable;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.Connection;

import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ApplicationProfile.StartupPolicy;
import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ProfileException;

import net.dpml.depot.Main;
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

    private ActivationGroupID m_rootID;

   /**
    * Creation of a station instance.
    *
    * @param logger the assigned logging channel
    * @param args supplimentary command line arguments
    * @exception RemoteException if a remote exception occurs during establishment
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
            URI uri = new URI( DEPOT_PROFILE_URI );
            m_model = (ApplicationRegistry) repository.getPlugin( classloader, uri, new Object[]{ logger } );

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
        if( Main.isOptionPresent( m_args, "-port" ) )
        {
            String value = Main.getOption( m_args, "-port" );
            m_args = Main.consolidate( m_args, "-port", 1 );
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

    public ApplicationRegistry getApplicationRegistry()  
    {
        return m_model;
    }

    public Application addApplication( ApplicationProfile profile )
      throws DuplicateKeyException, RemoteException
    {
        throw new UnsupportedOperationException( "addApplication/1" );
    }

    public void removeApplication( String key ) throws UnknownKeyException, RemoteException
    {
        throw new UnsupportedOperationException( "removeApplication/1" );
    }

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

    public ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException
    {
        return m_model.getApplicationProfile( key );
    }

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

    public Registry getRegistry( Connection connection ) throws RemoteException 
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
