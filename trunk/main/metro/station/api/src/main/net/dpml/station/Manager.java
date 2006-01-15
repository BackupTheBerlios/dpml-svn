
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.lang.UnknownKeyException;

/**
 * Application management center.
 */
public interface Manager extends Remote
{
   /**
    * Return a string array containing info about the general setup of the station.
    * @return station configuration info
    * @exception RemoteException if a remote error occurs
    */
    String[] getInfo() throws RemoteException;
    
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
    * @exception RemoteException if a remote error occurs
    */
    void shutdown() throws RemoteException;

}

