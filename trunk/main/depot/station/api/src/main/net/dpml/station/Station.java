
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;

public interface Station extends Remote
{
    String STATION_KEY = "/dpml/station";

    //Application addApplication( ApplicationProfile profile ) throws DuplicateKeyException, RemoteException;

    //void removeApplication( String key ) throws UnknownKeyException, RemoteException;

    //String[] getApplicationKeys() throws RemoteException;

    ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException;

    //Application getApplication( String key ) throws UnknownKeyException, RemoteException;
}

