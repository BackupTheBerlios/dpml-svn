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

package net.dpml.composition.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.component.info.Type;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.model.ComponentModel;

import net.dpml.composition.event.EventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.Handler;
import net.dpml.part.HandlerException;
import net.dpml.part.HandlerRuntimeException;
import net.dpml.part.ControlException;
import net.dpml.part.ControlRuntimeException;
import net.dpml.part.Instance;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.model.Value;

/**
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentHandler extends EventProducer implements Handler
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final ComponentController m_controller;
    private final ComponentModel m_model;
    private final State m_graph;
    private final ClassLoader m_classloader;
    private final Class m_class;
    private final Type m_type;
    private final Class[] m_services;
    private final LifestylePolicy m_lifestyle;
    private final String m_path;
    private final PropertyChangeSupport m_support;
    private final URI m_uri;
    private final Map m_map;
    private boolean m_active = false;
    
    //private final StateMachine m_machine;
    //private Object m_instance;
    
    private DefaultInstance m_instance; // TODO: change to a weakhashmap of active instances
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------
    
    public ComponentHandler( 
      ClassLoader classloader, Logger logger, ComponentController control, ComponentModel model )
      throws RemoteException
    {
        super();
        
        m_classloader = classloader;
        m_logger = logger;
        m_controller = control;
        m_model = model;
        m_lifestyle = model.getLifestylePolicy();
        m_path = model.getContextPath();
        m_support = new PropertyChangeSupport( this );
        
        m_graph = model.getStateGraph();
        
        try
        {
            m_uri = new URI( "component:" + m_path );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to construct the component uri using the path [" 
              + m_path + "]";
            throw new ControlRuntimeException( error, e );
        }
        
        m_map = new Hashtable();
        String name = model.getName();
        File work = control.getWorkDirectory( this );
        File temp = control.getTempDirectory( this );
        
        m_map.put( "name", name );
        m_map.put( "path", m_path );
        m_map.put( "work", work );
        m_map.put( "temp", temp );
        m_map.put( "uri", getURI() );
        
        String classname = model.getImplementationClassName();
        try
        {
            m_class = control.loadComponentClass( classloader, classname );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load component class: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        try
        {
            m_type = control.loadType( m_class );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load component type: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        try
        {
            m_services = control.loadServiceClasses( this );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load a service class declared in component type: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        getLogger().debug( "component controller [" + this + "] established" );
    }
    
    //--------------------------------------------------------------------------
    // Handler
    //--------------------------------------------------------------------------
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    */
    public boolean isActive()
    {
        return m_active;
    }

   /**
    * Activate the component handler.
    * @param handler the runtime handler
    * @exception Exception if an activation error occurs
    */
    public void activate() throws HandlerException, InvocationTargetException
    {
        if( isActive() )
        {
            return;
        }
        
        getLogger().debug( "initiating activation" );
        try
        {
            if( m_model.getActivationPolicy().equals( ActivationPolicy.STARTUP ) )
            {
                m_instance = createNewInstance();
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception raised while attempting to access component activation policy.";
            throw new HandlerException( error, e );
        }
        m_active = true;
    }
    
   /**
    * Deactivate the component.
    * @exception Exception if an activation error occurs
    */
    public void deactivate()
    {
        if( !isActive() )
        {
            return;
        }
        
        getLogger().debug( "initiating deactivation" );
        if( null != m_instance )
        {
            m_instance.dispose();
            m_instance = null;
        }
        m_active = false;
    }
    
    public Instance getInstance() throws InvocationTargetException, HandlerException
    {
        if( isActive() )
        {
            if( m_lifestyle.equals( LifestylePolicy.SINGLETON ) )
            {
                if( m_instance == null )
                {
                    m_instance = createNewInstance();
                }
                return m_instance;
            }
            else if( m_lifestyle.equals( LifestylePolicy.THREAD ) )
            {
                // TODO: add per-thread policy support
                final String error = 
                  "Per thread semantics are not supported in this revision.";
                throw new UnsupportedOperationException( error );
            }
            else if( m_lifestyle.equals( LifestylePolicy.TRANSIENT ) )
            {
                return createNewInstance();
            }
            else
            {
                final String error = 
                  "Unsuppported lifestyle policy: " + m_lifestyle;
                throw new UnsupportedOperationException( error );
            }
        }
        else
        {
            final String error = 
              "Component handler ["
              + this
              + "] is not active.";
            throw new IllegalStateException( error );
        }
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
    }
    
    //--------------------------------------------------------------------------
    // ComponentHandler
    //--------------------------------------------------------------------------
    
    Object getContextValue( String key ) throws ControlException
    {
        return m_controller.getContextValue( this, key );
    }

    State getStateGraph()
    {
        return m_graph;
    }
    
    Class getImplementationClass()
    {
        return m_class;
    }
    
    ClassLoader getClassLoader()
    {
        return m_classloader;
    }
    
    Handler getParentHandler()
    {
        return null;
    }
    
   /**
    * Returns the ccomponent type.
    * @return the type descriptor
    */
    Type getType()
    {
        return m_type;
    }
    
    Map getContextMap()
    {
        return m_map;
    }
    
    URI getURI()
    {
        return m_uri;
    }
    
    String getPath()
    {
        return m_path;
    }
    
    ComponentModel getComponentModel()
    {
        return m_model;
    }
    
    Class[] getServiceClassArray()
    {
        return m_services;
    }
    
    //--------------------------------------------------------------------------
    // internal
    //--------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private DefaultInstance createNewInstance() throws InvocationTargetException, HandlerException
    {
        try
        {
            Object object = m_controller.createInstance( this );
            Logger logger = getLogger();
            int id = System.identityHashCode( object );
            Logger log = logger.getChildLogger( "" + id );
            DefaultInstance instance = new DefaultInstance( this, log, object );
            getLogger().debug( "new instance: " + System.identityHashCode( instance ) );
            return instance;
        }
        catch( RemoteException e )
        {
            final String error = 
              "Cannot activate component due to remote exception.";
            throw new HandlerException( error, e );
        }
        catch( ControlException e )
        {
            final String error = 
              "Cannot activate component due to a controller related error.";
            throw new HandlerException( error, e );
        }
    }
    
    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------

    public String toString()
    {
        try
        {
            return "component:" + m_path + " (" + m_model.getImplementationClassName() + ")";
        }
        catch( RemoteException e )
        {
            return "handler:" + getClass().getName();
        }
    }
}
