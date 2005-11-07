/*
 * Copyright 2004-2005 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

import net.dpml.transit.PID;

import net.dpml.part.Context;

import net.dpml.profile.model.ApplicationProfile;

/**
 * Management model of an application.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Application extends Remote
{
   /**
    * Constant enumerator of a READY state.
    */
    State READY = new State( 0, "ready", "Ready" );
    
   /**
    * Constant enumerator of a STARTING state.
    */
    State STARTING = new State( 1, "starting", "Starting" );
    
   /**
    * Constant enumerator of a RUNNING state.
    */
    State RUNNING = new State( 2, "running", "Running" );
    
   /**
    * Constant enumerator of a STOPPING state.
    */
    State STOPPING = new State( 3, "stopping", "Stopping" );
    
   /**
    * Return the application context. 
    * (useage needs to be checked)
    * @return the application context
    * @exception RemoteException if a rmote error occurs
    */
    Context getContext() throws RemoteException;

   /**
    * Return the profile associated with this application 
    * (useage needs to be checked - is this really neeeded)
    * @return the application context
    * @exception RemoteException if a rmote error occurs
    */
    ApplicationProfile getProfile() throws RemoteException;

   /**
    * Return the cuirrent deployment state of the process.
    * @return the current process state
    * @exception RemoteException if a rmote error occurs
    */
    State getState() throws RemoteException;

   /**
    * Start the application.
    * @exception RemoteException if a remote error occurs
    */
    void start() throws RemoteException;

   /**
    * Method invoked by a process following establishment signalling
    * the process id.
    * @param pid the process identifier
    * @exception RemoteException if a rmote error occurs
    */
    void handleCallback( PID pid ) throws RemoteException;

   /**
    * Return the process identifier of the process within which the 
    * application is running.
    * @return the pid
    * @exception RemoteException if a rmote error occurs
    */
    PID getPID() throws RemoteException;

   /**
    * Stop the application.
    * @exception RemoteException if a rmote error occurs
    */
    void stop() throws RemoteException;

   /**
    * Restart the application.
    * @exception RemoteException if a rmote error occurs
    */
    void restart() throws RemoteException;

   /**
    * Add an application listener.
    * @param listener the listener to add
    * @exception RemoteException if a rmote error occurs
    */
    void addApplicationListener( ApplicationListener listener ) throws RemoteException;
    
   /**
    * Remove an application listener.
    * @param listener the listener to remove
    * @exception RemoteException if a rmote error occurs
    */
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

