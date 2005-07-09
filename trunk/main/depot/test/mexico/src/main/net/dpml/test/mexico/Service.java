
package net.dpml.test.mexico; 

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote 
{
    public Object info() throws RemoteException;

    public void addListener( Listener listener ) throws RemoteException;

    public void removeListener( Listener listener ) throws RemoteException;
}
