
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

import net.dpml.profile.model.ApplicationRegistry;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Application management center.
 */
public interface Manager extends Remote
{
   /**
    * Return the application registry.
    * @return the registry
    * @exception RemoteException if a remote error occurs
    */
    ApplicationRegistry getApplicationRegistry() throws RemoteException;

   /**
    * Return an application reference for the supplied key.
    * @param key the application key
    * @return the application
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    Application getApplication( String key ) throws UnknownKeyException, RemoteException;
    
   /**
    * Shutdown the station.
    */
    void shutdown() throws RemoteException;

}

