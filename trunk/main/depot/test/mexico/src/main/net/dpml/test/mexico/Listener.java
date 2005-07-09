
package net.dpml.test.mexico; 

import java.util.EventListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Listener extends EventListener, Remote
{
    void notify( Event event ) throws RemoteException;
}
