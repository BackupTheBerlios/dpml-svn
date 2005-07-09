
package net.dpml.test;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import net.dpml.test.mexico.Service;
import net.dpml.test.mexico.Listener;
import net.dpml.test.mexico.Event;
import net.dpml.test.mexico.ClientListener;

public class Client
{
    public Client( ) throws Exception
    {
        Registry registry = LocateRegistry.getRegistry();
        String[] names = registry.list();
        for( int i=0; i<names.length; i++ )
        {
            System.out.println( "REG: " + names[i] );
        }

        Service service = (Service) registry.lookup( "mexico" );
        ClientListener listener = new ClientListener();
        service.addListener( listener );
        try
        {
            service.info();
        }
        catch( RemoteException e )
        {
            System.out.println( "REMOTE EXCEPTION" );
            throw e;
        }
        service.removeListener( listener );
    }
}
