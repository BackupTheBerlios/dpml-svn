/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.station.exec;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.InvocationTargetException;

import net.dpml.component.Component;
import net.dpml.component.ControlException;
import net.dpml.component.Provider;
import net.dpml.component.Service;
import net.dpml.component.ServiceNotFoundException;
import net.dpml.component.ActivationPolicy;

import net.dpml.lang.Logger;

/**
 * The AbstractAdapter class is a minimalistic implementation of a part 
 * handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AbstractAdapter extends UnicastRemoteObject implements Component
{
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private final Logger m_logger;
    
    private boolean m_activated = false;
    
    //------------------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------------------
    
   /**
    * Creation of a new abstract handler.
    * @param logger the assigned logging channel
    * @exception RemoteException if a remote exception occurs
    */
    public AbstractAdapter( Logger logger ) throws RemoteException
    {
        super();
        m_logger = logger;
    }
    
    //------------------------------------------------------------------------------
    // Component
    //------------------------------------------------------------------------------
    
   /**
    * Get the activation policy.  If the activation policy is STARTUP, an implementation
    * a handler shall immidiately activation a runtime instance.  If the policy is on DEMAND
    * an implementation shall defer activiation until an explicit request is received.  If 
    * the policy if SYSTEM activation may occur at the discretion of an implementation.
    *
    * @return the activation policy
    * @exception RemoteException if a remote exception occurs
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    public ActivationPolicy getActivationPolicy() throws RemoteException
    {
        return ActivationPolicy.SYSTEM;
    }

   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service descriptor
    * @return a component matching the requested service
    * @exception ServiceNotFoundException if the request service cannot be resolved
    * @exception RemoteException if a remote exception occurs
    */
    public Component lookup( Service service ) throws ServiceNotFoundException, RemoteException
    {
        throw new UnsupportedOperationException( "lookup/1" );
    }
    
   /**
    * Initiate activation of the runtime handler.
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation instantiation exception occurs
    * @exception RemoteException if a remote exception occurs
    */
    public void commission() throws ControlException, InvocationTargetException, RemoteException
    {
        m_activated = true;
    }
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isActive() throws RemoteException
    {
        return m_activated;
    }
    
   /**
    * Return the number of instances currently under management.
    * @return the instance count.
    * @exception RemoteException if a remote exception occurs
    */
    public int size() throws RemoteException
    {
        return 0;
    }
    
   /**
    * Return a reference to a instance of the component handled by the handler.
    * @return the instance manager
    * @exception InvocationTargetException if the component instantiation process 
    *  is on demand and an target invocation error occurs
    * @exception ControlException if the component could not be established due to a handler 
    *  related error
    * @exception RemoteException if a remote exception occurs
    */
    public Provider getProvider() throws ControlException, InvocationTargetException, RemoteException
    {
        throw new UnsupportedOperationException( "getProvider/0" );
    }
    
   /**
    * Deactivate the handler.
    * @exception RemoteException if a remote exception occurs
    */
    public void decommission() throws RemoteException
    {
        m_activated = false;
    }
    
   /**
    * Test is this component can provide the requested service.
    * @param service the service defintion
    * @return true if the handler supports the requested service
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isaCandidate( Service service ) throws RemoteException
    {
        throw new UnsupportedOperationException( "isaCandidate/1" );
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
}
