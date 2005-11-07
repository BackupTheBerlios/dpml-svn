
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.profile.model.ApplicationProfile;
import net.dpml.profile.model.ApplicationRegistry;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Application management center.
 */
public interface Station extends Remote
{
   /**
    * Standard key for station lookup.
    */
    String STATION_KEY = "/dpml/station";

   /**
    * Return an array of application names managed by the station.
    * @return the application names
    * @exception RemoteException if a remote error occurs
    */
    String[] getApplicationKeys() throws RemoteException;

   /**
    * Return the application registry.
    * @return the registry
    * @exception RemoteException if a remote error occurs
    */
    ApplicationRegistry getApplicationRegistry() throws RemoteException;

   /**
    * Return the application profile.
    * @param key the profile key
    * @return the named profile
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Return a named application.
    * @param key the application key
    * @return the named aplication
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote error occurs
    */
    Application getApplication( String key ) throws UnknownKeyException, RemoteException;

    //Application addApplication( ApplicationProfile profile ) throws DuplicateKeyException, RemoteException;
    //void removeApplication( String key ) throws UnknownKeyException, RemoteException;

}

