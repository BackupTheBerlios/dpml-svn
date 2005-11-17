
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Application management center.
 */
public interface Station extends Remote
{
    static final String STATION_KEY = "dpml/station";
    
   /**
    * Return an application reference for the supplied callback id.
    * @param id the callback id
    * @return the application
    * @exception UnknownKeyException if the id is unknown
    * @exception RemoteException if a remote error occurs
    */
    Callback getCallback( String id ) throws UnknownKeyException, RemoteException;

}

