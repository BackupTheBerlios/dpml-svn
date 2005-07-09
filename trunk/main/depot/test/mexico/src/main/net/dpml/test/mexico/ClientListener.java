
package net.dpml.test.mexico; 

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientListener extends UnicastRemoteObject implements Listener
{
    private Logger m_logger = Logger.getLogger( "depot.station.client" );

    public ClientListener() throws RemoteException
    {
        super();
    }

    public void notify( Event event )
    {
        getLogger().log( Level.INFO, "count: " + event.getCount() );
    }

    private Logger getLogger()
    {
        return m_logger;
    }

}
