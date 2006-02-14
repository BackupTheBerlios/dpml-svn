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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Enumeration;

import net.dpml.station.ApplicationException;

import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.data.ValueDirective;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.ControlException;
import net.dpml.part.ServiceNotFoundException;
import net.dpml.part.Controller;
import net.dpml.part.ControllerContext;
import net.dpml.part.InitialContext;
import net.dpml.part.Component;
import net.dpml.part.Provider;
import net.dpml.part.Model;
import net.dpml.part.Service;

import net.dpml.metro.ComponentModelManager;
import net.dpml.metro.ContextModelManager;

import net.dpml.configuration.Configurable;
import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfigurationBuilder;

import net.dpml.transit.Logger;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * The ComponentAdapter provides support for the establishment of a part
 * controller and delegation of handler requests to the part handler
 * resolved from its associated controller.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentAdapter extends AbstractAdapter
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
    private Provider m_instance;
    
    //------------------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------------------
    
   /**
    * Creation of a new component adapter.
    * @param logger the assigned logging channel
    * @param codebase the codebase uri
    * @param config a configuration uri
    * @param params a parameters uri
    * @param categories a categories uri
    * @param properties context override properties
    * @exception Exception if an error occurs
    */
    public ComponentAdapter( 
      Logger logger, URI codebase, URI config, URI params, URI categories, Properties properties ) throws Exception
    {
        super( logger );
        
        m_codebase = codebase;
        
        try
        {
            ClassLoader classloader = Controller.class.getClassLoader();
            URI uri = new URI( "@COMPOSITION-CONTROLLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            InitialContext context = new InitialContext( logger );
            Constructor constructor = c.getConstructor( new Class[]{ControllerContext.class} );
            m_controller = (Controller) constructor.newInstance( new Object[]{context} );
        }
        catch( Exception e )
        {
            final String error =
              "Internal error while attempting to establish the standard part controller.";
            throw new ApplicationException( error, e );
        }
        
        Model model = m_controller.createModel( codebase );
        m_component = m_controller.createComponent( model );
        
        if( null != config )
        {
            if( model instanceof Configurable )
            {
                logger.info( "applying configuration: " + config );
                try
                {
                    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                    URL url = config.toURL();
                    InputStream input = url.openStream();
                    Configuration configuration = builder.build( input );
                    Configurable configurable = (Configurable) model;
                    configurable.configure( configuration );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Error loading configuration: " + config;
                    throw new ApplicationException( error, e );
                }
            }
            else
            {
                final String error = 
                  "Cannot apply a configuration to a non-configurable managment context."
                  + "\nManagement Context: " + model.getClass().getName();
                throw new ApplicationException( error );
            }
        }
        
        if( null != params )
        {
            throw new UnsupportedOperationException( "Parameters not currently supported." );
        }
        
        if( model instanceof ComponentModelManager )
        {
            ComponentModelManager componentModel = (ComponentModelManager) model;
            ContextModelManager cm = componentModel.getContextManager();
            Enumeration names = properties.propertyNames();
            while( names.hasMoreElements() )
            {
                String key = (String) names.nextElement();
                String value = properties.getProperty( key );
                EntryDescriptor entry = cm.getEntryDescriptor( key );
                String classname = entry.getClassname();
                ValueDirective v = new ValueDirective( classname, value );
                cm.setEntryDirective( key, v );
            }
        }
        else
        {
            final String error = 
              "Context class not recognized: " 
              + model.getClass().getName();
            throw new UnsupportedOperationException( error );
        }
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
        return m_component.getActivationPolicy();
    }
    
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
        m_instance = m_component.getProvider();
        m_object = m_component.getProvider().getValue( false );
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
    public Provider getProvider() throws ControlException, InvocationTargetException, RemoteException
    {
        return m_component.getProvider();
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
