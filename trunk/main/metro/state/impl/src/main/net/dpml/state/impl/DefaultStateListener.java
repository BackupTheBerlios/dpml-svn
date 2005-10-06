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

package net.dpml.state.impl;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.EventListener;

import net.dpml.state.State;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;


/**
 * State listener that listens to a remote state change source and propergates
 * events locally as PropertyChangeEvent instances.   Consumers should register 
 * PropertyChangeListener instances and track 'state' events as required.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class DefaultStateListener extends UnicastRemoteObject implements StateListener
{
    private static final String PROPERTY_NAME = "state";
    
    private final PropertyChangeSupport m_support;
    
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
    
    public void addPropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.addPropertyChangeListener( listener );
    }
    
    public void removePropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.removePropertyChangeListener( listener );
    }
}

