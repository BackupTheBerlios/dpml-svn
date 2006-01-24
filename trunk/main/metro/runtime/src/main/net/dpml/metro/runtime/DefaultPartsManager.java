/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.runtime;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.logging.Logger;

import net.dpml.metro.model.ComponentModel;

import net.dpml.part.ControlException;
import net.dpml.part.remote.Component;
import net.dpml.part.remote.Model;
import net.dpml.metro.control.ComponentManager;
import net.dpml.metro.control.ComponentHandler;

import net.dpml.lang.UnknownKeyException;

/**
 * Default implementation of the local Parts interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultComponentManager implements ComponentManager
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component handler.
    */
    private final DefaultComponentHandler m_handler;
    
   /**
    * The logging channel.
    */
    private final Logger m_logger;
    
   /**
    * The internal map of component handlers.
    */
    private final Map m_handlers = new Hashtable();
    
    private boolean m_commissioned = false;
        
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a new instance handler.
    *
    * @param handler the component handler
    * @param logger the logging channel
    */
    DefaultComponentManager( ComponentController control, DefaultComponentHandler handler, Logger logger ) 
      throws ControlException, RemoteException
    {
        m_handler = handler;
        m_logger = logger;
        
        ClassLoader classloader = handler.getClassLoader();
        ComponentModel model = handler.getComponentModel();
        String[] keys = model.getPartKeys();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            try
            {
                ComponentModel m = model.getComponentModel( key );
                Component h = control.createDefaultComponentHandler( handler, classloader, m, true );
                m_handlers.put( key, h );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Invalid part key ["
                  + key
                  + "] in component ["
                  + handler
                  + "]";
                throw new ControllerRuntimeException( error, e );
            }
            catch( Exception e )
            {
                final String error = 
                  "Internal error while attempting to create a subsidiary part ["
                  + key
                  + "] in component ["
                  + handler
                  + "]";
                throw new ControllerRuntimeException( error, e );
            }
        }
    }
    
    private ComponentModel getComponentModel()
    {
        return m_handler.getComponentModel();
    }
    
    //-------------------------------------------------------------------
    // ComponentManager
    //-------------------------------------------------------------------

   /**
    * Return the array of keys used to idenetity internal parts.
    * @return the part key array
    */
    public String[] getKeys()
    {
        return (String[]) m_handlers.keySet().toArray( new String[0] );
    }
    
   /**
    * Return a component handler.
    * @param key the internal component key
    * @return the local component handler
    */
    public synchronized Component getComponent( String key ) throws UnknownKeyException
    {
        if( m_handlers.containsKey( key ) )
        {
            return (Component) m_handlers.get( key );
        }
        else
        {
            throw new UnknownKeyException( key );
        }
    }
    
   /**
    * Return the component model for the supplied component.
    * @param component the component
    * @return the component model
    */
    public Model getComponentModel( Component component )
    {
        if( component instanceof DefaultComponentHandler )
        {
            DefaultComponentHandler handler = (DefaultComponentHandler) component;
            return handler.getComponentModel();
        }
        else
        {
            final String error = 
              "Component ["
              + component 
              + "] is not castable to net.dpml.metro.runtime.DefaultComponentHandler.";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Return a component handler.
    * @param key the internal component key
    * @return the local component handler
    */
    public synchronized ComponentHandler getComponentHandler( String key ) throws UnknownKeyException
    {
        if( m_handlers.containsKey( key ) )
        {
            return (ComponentHandler) m_handlers.get( key );
        }
        else
        {
            throw new UnknownKeyException( key );
        }
    }
    
   /**
    * Return the commissioned state of the part collection.
    * @return true if commissioned else false
    */
    public boolean isCommissioned()
    {
        return m_commissioned;
    }
    
   /**
    * Initiate the ordered activation of all internal parts.
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation source exception occured
    * @exception RemoteException if a remote exception occurs
    */
    public synchronized void commission() throws ControlException
    {
        if( m_commissioned )
        {
            final String error = 
              "Illegal attempt to commission a part manager that is already commissioned."
              + "Component: " + m_handler;
            throw new IllegalStateException( error );
        }
        
        getLogger().debug( "commissioning" );
        ArrayList list = new ArrayList();
        Component[] components = getComponents();
        ControlException exception = null;
        for( int i=0; i<components.length; i++ )
        {
            Component component = components[i];
            try
            {
                component.activate();
                list.add( component );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Error during the commission of the internal parts of a component."
                  + "\nEnclosing Component: " + m_handler
                  + "\nInternal Part: " + component;
                  exception = new ControllerException( error, e );
                break;
            }
        }
        
        if( null != exception )
        {
            Component[] selection = (Component[]) list.toArray( new Component[0] );
            decommission( selection );
            throw exception;
        }
        else
        {
            m_commissioned = true;
        }
    }
    
   /**
    * Initiate deactivation of all internal parts.
    */
    public synchronized void decommission()
    {
        if( !m_commissioned )
        {
            final String error = 
              "Illegal attempt to decommission a part manager that is already decommissioned."
              + "Component: " + m_handler;
            throw new IllegalStateException( error );
        }
        
        getLogger().debug( "decommissioning" );
        Component[] components = getComponents();
        decommission( components );
    }
    
   /**
    * Initiate deactivation of all internal parts.
    */
    private void decommission( Component[] components )
    {
        try
        {
            int n = components.length -1;
            for( int i=n; i>-1; i-- )
            {
                Component component = components[i];
                try
                {
                    component.deactivate();
                }
                catch( RemoteException e )
                {
                    final String message = 
                      "Ignoring remote exception raised during deactivation.";
                    getLogger().warn( message, e );
                }
            }
        }
        finally
        {
            m_commissioned = false;
        }
    }

    Component[] getComponents()
    {
        return (Component[]) m_handlers.values().toArray( new Component[0] );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
