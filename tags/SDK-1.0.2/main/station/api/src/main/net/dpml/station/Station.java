
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.lang.UnknownKeyException;

/**
 * Application management center.
 */
public interface Station extends Remote
{
   /**
    * The name under which the station will be published in 
    * an RMI registry.
    */
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

