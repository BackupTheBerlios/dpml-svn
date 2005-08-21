
package net.dpml.depot.station; 

import java.net.URI;
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
import java.util.prefs.Preferences;
import java.util.LinkedList;
import java.util.Properties;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.Connection;

import net.dpml.profile.DepotProfile;
import net.dpml.profile.ProfileException;
import net.dpml.profile.ActivationProfile;
import net.dpml.profile.ActivationGroupProfile;

import net.dpml.depot.GeneralException;

/**
 * The DepotStation class provides support for the establishment and maintenance
 * of DPML server processes.  Each process represents an identifiable virtual machine
 * and associated component model. 
 */
public class DepotStation extends UnicastRemoteObject implements Station
{
    public static final String STATION = "dpml:station";

    private int m_count = 0;
    private final Logger m_logger;
    private Hashtable m_table = new Hashtable();

    private DepotProfile m_model;

    private ActivationGroupID m_rootID;

    public DepotStation( Logger logger, Preferences prefs, String[] args ) throws RemoteException
    {
        super();

        try
        {
            Repository repository = Transit.getInstance().getRepository();
            ClassLoader classloader = DepotStation.class.getClassLoader();
            URI uri = new URI( DEPOT_PROFILE_URI );
            m_model = (DepotProfile) repository.getPlugin( classloader, uri, new Object[]{ prefs, logger } );

            m_logger = logger;

            //
            // startup the general registry
            //
            
            int port = Registry.REGISTRY_PORT;
            Connection connection = new Connection( null, port, true, true );
            Registry registry = getRegistry( connection );

            //
            // register ourself into the registry
            //

            try
            {
                registry.bind( STATION, this );
            }
            catch( AlreadyBoundException e )
            {
                final String error = 
                  "Another instance of the Depot Station is already bound to port: " + port;
                throw new GeneralException( error );
            }

            String[] list = registry.list();
            logger.info( "registry: " + list.length );
            for( int i=0; i<list.length; i++ )
            {
                logger.info( "entry: " + list[i] );
            }

            /*
            // get groups and initiate deployment
            ActivationGroupProfile[] profiles = m_model.getActivationGroupProfiles();
            for( int i=0; i<profiles.length; i++ )
            {
                ActivationGroupProfile profile = profiles[i];
                ActivationGroupID id = deployActivationGroupProfile( profile );
                m_table.put( profile, id );
            }
            */
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

    private ActivationGroupID deployActivationGroupProfile( ActivationGroupProfile group )
      throws RemoteException
    {
        String gid = group.getID();
        ActivationGroupID id = null;
        try
        {
            ActivationGroupDesc desc = createActivationGroupDesc( group );
            id = ActivationGroup.getSystem().registerGroup( desc );
        }
        catch( Exception e )
        {
            final String error =
              "Internal error while attempting to register an activation group.";
            throw new ServerException( error, e );
        }

        ActivationProfile[] profiles = group.getActivationProfiles();
        for( int i=0; i<profiles.length; i++ )
        {
            ActivationProfile profile = profiles[i];
            String aid = profile.getID();
            try
            {
                registerActivatable( id, profile );
            }
            catch( Exception e )
            {
                final String error = 
                  "An error occured while attempt to deploy an activation profile."
                  + "\nGroup Profile: " + gid
                  + "\nActivatable Profile: " + aid;
                getLogger().error( error, e );
            }
        }

        return id;
    }

    private ActivationGroupDesc createActivationGroupDesc( ActivationGroupProfile profile )
      throws RemoteException
    {
        String id = profile.getID();
        try
        {
            String[] args = new String[0];
            Properties properties = profile.getSystemProperties();
            CommandEnvironment environment = new CommandEnvironment( null, args ) ;
            return new ActivationGroupDesc( properties, environment );
        }
        catch( Exception e )
        {
            final String error =
              "Internal error while attempting to construct an activation group descriptor."
              + "\nGroup ID: " + id;
            throw new ServerException( error, e );
        }
    }

    private void registerActivatable( 
      ActivationGroupID group, ActivationProfile profile ) 
      throws RemoteException
    {
        String id = profile.getID();
        String location = profile.getCodeBaseURI().toASCIIString();
        String classname = profile.getClassname();
        MarshalledObject data = null;

        final String message = 
          "Deploying activation profile: " + id
          + "\nCode Source: " + location
          + "\nClassname: " + classname;
        getLogger().info( message );

        try
        {
            ActivationDesc desc = new ActivationDesc( group, classname, location, data );
            Remote remote = Activatable.register( desc );
            getLogger().info( "Registered: " + id + " (" + remote.getClass().getName() + ")" );
            Naming.rebind( id, remote );
            getLogger().info( "binding complete" );
        }
        catch( Exception e )
        {
            getLogger().error( "error attempting to bind stub", e );
            final String error =
              "Registration error due to " + e.toString();
            throw new ServerException( error, e );
        }
        catch( Throwable e )
        {
            final String error =
              "Registration failed due to " + e.toString();
            Exception exception = new Exception( error, e );
            throw new ServerException( error, exception );
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
