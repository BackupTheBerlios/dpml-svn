
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.transit.PID;

import net.dpml.profile.ApplicationProfile;

public interface Application extends Remote
{
    ApplicationProfile getProfile() throws RemoteException;

    PID getPID() throws RemoteException;

    PID start() throws RemoteException;

    void stop() throws RemoteException;

    PID restart() throws RemoteException;
}

