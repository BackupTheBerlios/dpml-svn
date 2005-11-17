/* 
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.state.impl;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import net.dpml.metro.state.State;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.StateListener;


/**
 * State listener that listens to a remote state change source and propergates
 * events locally as PropertyChangeEvent instances.   Consumers should register 
 * PropertyChangeListener instances and track 'state' events as required.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class DefaultStateListener extends UnicastRemoteObject implements StateListener
{
    private static final String PROPERTY_NAME = "state";
    
    private final PropertyChangeSupport m_support;
    
   /**
    * Creation of a new state listener.
    * @exception RemoteException if a remote error occurs
    */
    public DefaultStateListener() throws RemoteException
    {
        super();
        m_support = new PropertyChangeSupport( this );
    }
    
   /**
    * Notify the listener of a state change.
    *
    * @param event the state change event
    */
    public void stateChanged( final StateEvent event )
    {
        final Object source = event.getSource();
        final State oldValue = event.getFromState();
        final State newValue = event.getToState();
        final PropertyChangeEvent e = 
          new PropertyChangeEvent( source, PROPERTY_NAME, oldValue, newValue );
        m_support.firePropertyChange( e );
    }
    
   /**
    * Add a local property change listener to this remote listener.
    * @param listener the property change listener to add
    */
    public void addPropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.addPropertyChangeListener( listener );
    }
    
   /**
    * Remove a local property change listener from this remote listener.
    * @param listener the property change listener to remove
    */
    public void removePropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.removePropertyChangeListener( listener );
    }
    
   /**
    * Compare this object to another for equality.
    * @param other the other object
    * @return true if the object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( getClass().equals( other.getClass() ) )
        {
            DefaultStateListener listener = (DefaultStateListener) other;
            return m_support.equals( listener.m_support );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hashcode for this instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        return m_support.hashCode();
    }
}

