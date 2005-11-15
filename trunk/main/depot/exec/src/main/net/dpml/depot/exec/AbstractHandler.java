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

package net.dpml.depot.exec;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.InvocationTargetException;

import net.dpml.part.Handler;
import net.dpml.part.HandlerException;
import net.dpml.part.Instance;

import net.dpml.transit.Logger;

/**
 * The Metro plugin handles the establishment of a part and optional 
 * invocatio of a callback to a central station.
 */
public class AbstractHandler extends UnicastRemoteObject implements Handler
{
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private final Logger m_logger;
    
    private boolean m_activated = false;
    
    //------------------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------------------
    
    public AbstractHandler( Logger logger ) throws RemoteException
    {
        super();
        m_logger = logger;
    }
    
    //------------------------------------------------------------------------------
    // Handler
    //------------------------------------------------------------------------------
    
   /**
    * Initiate activation of a runtime handler.
    * @exception HandlerException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation source exception occured
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
    * @return the instance holder
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
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    protected Logger getLogger()
    {
        return m_logger;
    }
}
