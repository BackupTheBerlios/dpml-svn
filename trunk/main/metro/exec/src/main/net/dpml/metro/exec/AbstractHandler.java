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

package net.dpml.metro.exec;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.InvocationTargetException;

import net.dpml.metro.part.Component;
import net.dpml.metro.part.HandlerException;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Service;
import net.dpml.metro.part.ServiceNotFoundException;

import net.dpml.transit.Logger;

/**
 * The AbstractHandler class is a minimalistic implementation of a part 
 * handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AbstractHandler extends UnicastRemoteObject implements Component
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
    public AbstractHandler( Logger logger ) throws RemoteException
    {
        super();
        m_logger = logger;
    }
    
    //------------------------------------------------------------------------------
    // Component
    //------------------------------------------------------------------------------
    
   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service descriptor
    * @exception ServiceNotFoundException if the request service cannot be resolved
    * @exception RemoteException if a remote exception occurs
    */
    public Component lookup( Service service ) throws ServiceNotFoundException, RemoteException
    {
        String classname = service.getServiceClass().getName();
        throw new ServiceNotFoundException( classname );
    }
    
   /**
    * Initiate activation of the runtime handler.
    * @exception HandlerException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation instantiation exception occurs
    * @exception RemoteException if a remote exception occurs
    */
    public void activate() throws HandlerException, InvocationTargetException, RemoteException
    {
        if( m_activated )
        {
            return;
        }
        getLogger().info( "activation" );
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
    * @exception HandlerException if the component could not be established due to a handler 
    *  related error
    * @exception RemoteException if a remote exception occurs
    */
    public Instance getInstance() throws HandlerException, InvocationTargetException, RemoteException
    {
        throw new UnsupportedOperationException( "getInstance/0" );
    }
    
   /**
    * Deactivate the handler.
    * @exception RemoteException if a remote exception occurs
    */
    public void deactivate() throws RemoteException
    {
        if( !m_activated )
        {
            return;
        }
        getLogger().info( "deactivation" );
        m_activated = false;
    }
    
   /**
    * Test is this component can provide the requested service.
    * @param service the service defintion
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isaCandidate( Service service ) throws RemoteException
    {
        return false;
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    protected Logger getLogger()
    {
        return m_logger;
    }
}
