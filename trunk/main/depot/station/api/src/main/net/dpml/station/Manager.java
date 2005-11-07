
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.profile.info.ApplicationDescriptor;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Application management center.
 */
public interface Manager
{
   /**
    * Return the runtime station.
    * @return the station
    */
    Station getStation();
    
   /**
    * Set the application descriptor assigned to the supplied key.  If the 
    * key matches an existing application key, the current descriptor will be 
    * updated to the new value.  If no key is present a new key will be created 
    * and the supplied descriptor will be assigned as the application profile.
    * Modification to key/descript bindings will take effect following application
    * startup or redeployment.
    *
    * @param key the application key
    * @param descriptor the application descriptor
    * @exception RemoteException if a remote error occurs
    */
    void setApplicationDescriptor( String key, ApplicationDescriptor descriptor ) 
      throws RemoteException;

}

