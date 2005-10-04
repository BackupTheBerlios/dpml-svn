
package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

import net.dpml.transit.PID;

import net.dpml.profile.ApplicationProfile;

public interface Application extends Remote
{
    State READY = new State( 0, "ready", "Ready" );
    State STARTING = new State( 1, "starting", "Starting" );
    State RUNNING = new State( 2, "running", "Running" );
    State STOPPING = new State( 3, "stopping", "Stopping" );
    
    Object getManagementContext() throws RemoteException;

    ApplicationProfile getProfile() throws RemoteException;

    State getState() throws RemoteException;

    void start() throws RemoteException;

    void handleCallback( PID pid ) throws RemoteException;

    PID getPID() throws RemoteException;

    void stop() throws RemoteException;

    void restart() throws RemoteException;

    void addApplicationListener( ApplicationListener listener ) throws RemoteException;
    
    void removeApplicationListener( ApplicationListener listener ) throws RemoteException;

   /**
    * Process state enumeration.
    */
    public static final class State implements Serializable
    {
        private final int m_index;
        private final String m_label;
        private final String m_key;

       /**
        * Internal constructor.
        * @param index the enumeration index.
        */
        private State( int index, final String key, final String label )
        {
            m_index = index;
            m_label = label;
            m_key = key;
        }

       /**
        * Return the key used to identify this state instance.
        * @return the key
        */
        public String key()
        {
            return m_key;
        }

       /**
        * Test this policy for equality with the supplied instance.
        * @param other the object to test against
        * @return true if the instances are equivalent
        */
        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            else if( other.getClass() == State.class )
            {
                State state = (State) other;
                return state.m_index == m_index;
            }
            else
            {
                return false;
            }
        }

       /**
        * Return the hascode for this instance.
        * @return the instance hashcode
        */
        public int hashCode()
        {
            return m_index;
        }

       /**
        * Return the string representation of this instance.
        * @return the string
        */
        public String toString()
        {
            return m_label;
        }
    }
}

