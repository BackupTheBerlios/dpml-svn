
package net.dpml.test.mexico; 

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationID;
import java.rmi.MarshalledObject;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.prefs.Preferences;

public class Server extends Activatable implements Service
{
    private int m_count = 0;
    private final Logger m_logger = Logger.getLogger( "mexico" );
    private LinkedList m_listeners = new LinkedList();

    public Server( ActivationID id, MarshalledObject data ) throws RemoteException
    {
        super( id, 0 );
        Preferences prefs = Preferences.userNodeForPackage( Server.class );
        m_count = prefs.getInt( "count", 0 );
    }

    public Object info() throws RemoteException 
    {
        int count = m_count;
        getLogger().info( "# " + count );
        postEvent( count );
        m_count++;
        return "" + count;
    }

    public void addListener( Listener listener ) 
    {
        m_listeners.add( listener );
    }

    public void removeListener( Listener listener ) 
    {
        m_listeners.remove( listener );
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private void postEvent( int count )
    {
        Event event = new Event( this, count );
        Listener[] listeners = (Listener[]) m_listeners.toArray( new Listener[0] );
        for( int i=0; i<listeners.length; i++ )
        {
            Listener listener = listeners[i];
            try
            {
                listener.notify( event );
            }
            catch( Throwable e )
            {
                getLogger().log( Level.SEVERE, e.getMessage(), e.getCause() );
            }
        }
    }
}
