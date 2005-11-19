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

import java.net.URI;
import java.rmi.RemoteException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.dpml.station.ApplicationException;

import net.dpml.metro.part.Controller;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.ControlException;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Context;
import net.dpml.metro.part.Part;
import net.dpml.metro.part.Service;
import net.dpml.metro.part.ServiceNotFoundException;

import net.dpml.transit.Logger;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * The ComponentHandler provides support for the establishment of a part
 * controller and delegation of handler requests to the part handler
 * resolved from its associated controller.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentHandler extends AbstractHandler
{
    //------------------------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------------------------
    
    private final URI m_codebase;
    
    private final Controller m_controller;
    
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private Component m_component;
    private Object m_object;
    private Instance m_instance;
    
    //------------------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------------------
    
   /**
    * Creation of a new component handler.
    * @param logger the assigned logging channel
    * @param codebase the codebase uri
    * @exception Exception if an error occurs
    */
    public ComponentHandler( Logger logger, URI codebase ) throws Exception
    {
        super( logger );
        
        m_codebase = codebase;
        
        try
        {
            ClassLoader classloader = Part.class.getClassLoader();
            URI uri = new URI( "@COMPOSITION-CONTROLLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{Logger.class} );
            m_controller = (Controller) constructor.newInstance( new Object[]{logger} );
        }
        catch( Exception e )
        {
            final String error =
              "Internal error while attempting to establish the standard part controller.";
            throw new ApplicationException( error, e );
        }
        
        Part part = m_controller.loadPart( codebase );
        Context context = m_controller.createContext( part );
        m_component = m_controller.createComponent( context );
    }

    //------------------------------------------------------------------------------
    // Component
    //------------------------------------------------------------------------------
    
   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service descriptor
    * @return a component matchi9ng the requested service
    * @exception ServiceNotFoundException if the service could not be resolved
    * @exception RemoteException if a remote exception occurs
    */
    public Component lookup( Service service ) throws ServiceNotFoundException, RemoteException
    {
        return m_component.lookup( service );
    }
    
   /**
    * Initiate activation of a runtime handler.
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation source exception occured
    * @exception RemoteException if a remote exception occurs
    */
    public void activate() throws ControlException, InvocationTargetException, RemoteException
    {
        m_component.activate();
        m_instance = m_component.getInstance();
        m_object = m_component.getInstance().getValue( false );
}
    
   /**
    * Return the number of instances currently under management.
    * @return the instance count.
    * @exception RemoteException if a remote exception occurs
    */
    public int size() throws RemoteException
    {
        return m_component.size();
    }
    
   /**
    * Return a reference to a instance of the component handled by the handler.
    * @return the instance holder
    * @exception InvocationTargetException if the component instantiation process 
    *  is on demand and an target invocation error occurs
    * @exception ControlException if the component could not be established due to a handler 
    *  related error
    * @exception RemoteException if a remote exception occurs
    */
    public Instance getInstance() throws ControlException, InvocationTargetException, RemoteException
    {
        return m_component.getInstance();
    }
    
   /**
    * Deactivate the handler.
    * @exception RemoteException if a remote exception occurs
    */
    public void deactivate() throws RemoteException
    {
        m_component.deactivate();
    }
    
   /**
    * Return true if this handler is a candidate for the supplied service defintion.
    * @param service the service definition
    * @return true if this is a candidate
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isaCandidate( Service service ) throws RemoteException
    {
        return m_component.isaCandidate( service );
    }
    
}
