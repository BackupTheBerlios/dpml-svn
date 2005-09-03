
package net.dpml.depot.desktop;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.dpml.depot.Handler;

import net.dpml.station.Station;
import net.dpml.station.Application;
import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.Logger;

/**
 * Application registry root tree node. 
 */
public final class ApplicationRegistryTreeNode extends Node
{
    private static final int ID_COLUMN = 0;
    private static final int CODEBASE_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;
    private static final ImageIcon ICON = readImageIcon( "16/folder.png" );

    private final Logger m_logger;
    private final Station m_station;
    private final JLabel m_label;
    private boolean m_responsibleForTheStation = false;

    public ApplicationRegistryTreeNode( Logger logger, String[] args ) throws Exception
    {
        super();

        m_logger = logger;

        //
        // when dealing with RMI accessible objects we need to make sure that the 
        // context classloader is established with a classloader referencing the API
        // of the classes we are referencing otherwise things will fail with a 
        // ClassNotFoundException
        //

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

        //
        // locate the station (creating it if is does not exist)
        // TODO: update this to display a progress bar as station loading can 
        // be a time consuming process
        //

        m_station = resolveStation();

        //
        // for all of the application profiles registered within the registry
        // add them as child nodes of this node
        // 

        String[] keys = m_station.getApplicationKeys();
        for( int i=0; i < keys.length; i++ )
        {
            final String key = keys[i];
            Application application = m_station.getApplication( key );
            add( new ApplicationProfileTreeNode( application ) );
        }

        m_label = new JLabel( "Registry", ICON, SwingConstants.LEFT );
        m_label.setBorder( new EmptyBorder( 3, 3, 2, 2 ) );
    }

    void dispose()
    {
        if( m_station instanceof Handler && m_responsibleForTheStation )
        {
            Handler handler = (Handler) m_station;
            handler.destroy();
        }
    }

    private Station resolveStation()
    {
        try
        {
            Registry registry = getRegistry( null, Registry.REGISTRY_PORT );
            return (Station) registry.lookup( Station.STATION_KEY );
        }
        catch( NotBoundException e )
        {
            return createStation();
        }
        catch( RemoteException e )
        {
            return createStation();
        }
    }

    private Station createStation()
    {
        m_responsibleForTheStation = true;
        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = getClass().getClassLoader();
        try
        {
            URI uri = new URI( "@STATION-URI@" );
            return (Station) repository.getPlugin( classloader, uri, new Object[]{ m_logger } );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to establish the Station.";
            throw new RuntimeException( error, e );
        }
    }

    public Registry getRegistry( String host, int port ) throws RemoteException 
    {
        if( ( null == host ) || ( "localhost".equals( host ) ) )
        {
            return LocateRegistry.getRegistry( port );
        }
        else
        {
            return LocateRegistry.getRegistry( host, port );
        }
    }

    Component getLabel()
    {
        return m_label;
    }
}