/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import net.dpml.component.ActivationPolicy;
import net.dpml.component.ControlException;
import net.dpml.component.Component;
import net.dpml.component.Model;
import net.dpml.component.Service;
import net.dpml.component.Directive;
import net.dpml.component.Disposable;

import net.dpml.lang.Classpath;
import net.dpml.lang.UnknownKeyException;
import net.dpml.lang.Version;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;
import net.dpml.metro.ComponentModel;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ComponentDirective;

/**
 * Internal parts manager.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultPartsManager implements PartsManager, Disposable
{
    private final DefaultProvider m_provider;
    private final String[] m_keys;
    private final ComponentHandler[] m_components;
    
    private boolean m_commissioned;
    
    DefaultPartsManager( DefaultProvider provider ) throws RemoteException, IOException, ControlException
    {
        m_provider = provider;
        
        DefaultComponentHandler handler = provider.getDefaultComponentHandler();
        ClassLoader classloader = handler.getClassLoader();
        ComponentModel model = handler.getComponentModel();
        String partition = model.getContextPath();
        final String base = partition + Model.PARTITION_SEPARATOR;
        PartReference[] references = model.getPartReferences();
        m_keys = new String[ references.length ];
        m_components = new ComponentHandler[ references.length ];
        
        if( references.length > 0 )
        {
            if( provider.getLogger().isTraceEnabled() )
            {
                provider.getLogger().trace( "building internal parts" );
            }
        }
        
        for( int i=0; i < references.length; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            m_keys[i] = key;
            Directive part = ref.getDirective();
            if( provider.getLogger().isTraceEnabled() )
            {
                provider.getLogger().trace( "building part: " + key );
            }
            if( part instanceof ComponentDirective )
            {
                try
                {
                    ComponentDirective directive = (ComponentDirective) part;
                    Classpath classpath = resolveClasspath( directive );
                    ComponentController controller = handler.getComponentController();
                    ComponentModel manager = 
                      controller.createComponentModel( classloader, classpath, base, directive, key );
                    ComponentHandler component = 
                      controller.createDefaultComponentHandler( m_provider, classloader, manager, true );
                    m_components[i] = component;
                }
                catch( Exception e )
                {
                    final String error = 
                      "Internal error while attempting to create a subsidiary part ["
                      + key
                      + "] in ["
                      + m_provider
                      + "]";
                    throw new ControllerRuntimeException( error, e );
                }
            }
            else
            {
                final String error = 
                  "Component directive class [" 
                  + part.getClass() 
                  + "] not recognized.";
                throw new UnsupportedOperationException( error );
            }
        }
    }
    
    //-------------------------------------------------------------------
    // Disposable
    //-------------------------------------------------------------------

    public void dispose()
    {
        decommission();
        String[] keys = getKeys();
        for( int i=keys.length-1; i>-1; i-- )
        {
            try
            {
                String key = keys[i];
                ComponentHandler handler = getComponentHandler( key );
                if( handler instanceof Disposable )
                {
                    Disposable disposable = (Disposable) handler;
                    disposable.dispose();
                }
            }
            catch( UnknownKeyException e )
            {
                e.printStackTrace(); // will not happen
            }
        }
    }

    //-------------------------------------------------------------------
    // PartsManager
    //-------------------------------------------------------------------
    
   /**
    * Return the array of keys used to identify internal parts.
    * @return the part key array
    */
    public String[] getKeys()
    {
        return m_keys;
    }
    
   /**
    * Return an array of all component handlers.
    * @return the local component handler array
    */  
    public ComponentHandler[] getComponentHandlers()
    {
        return m_components;
    }

   /**
    * Return a component handler.
    * @param key the internal component key
    * @return the component handler
    */
    public synchronized Component getComponent( String key ) throws UnknownKeyException
    {
        return getComponentHandler( key );
    }

   /**
    * Return an array of component handlers assignable to the supplied service.
    * @param clazz the service class to match against
    * @return the local component handler array
    */
    public ComponentHandler[] getComponentHandlers( Class clazz )
    {
        Service service = new DefaultService( clazz, Version.parse( "-1" ) );
        ArrayList list = new ArrayList();
        ComponentHandler[] components = getComponentHandlers();
        for( int i=0; i<components.length; i++ )
        {
            ComponentHandler component = components[i];
            try
            {
                if( component.isaCandidate( service ) )
                {
                    list.add( component );
                }
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unexpected remote exception raised during subsidiary component evaluation."
                  + "\nProvider: " + m_provider;
                throw new ControllerRuntimeException( error, e );
            }
        }
        return (ComponentHandler[]) list.toArray( new ComponentHandler[0] );
    }
    
   /**
    * Return a component handler.
    * @param key the internal component key
    * @return the local component handler
    */
    public synchronized ComponentHandler getComponentHandler( String key ) throws UnknownKeyException
    {
        for( int i=0; i<m_keys.length; i++ )
        {
            String k = m_keys[i];
            if( k.equals( key ) )
            {
                return m_components[i];
            }
        }
        throw new UnknownKeyException( key );
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
            return;
        }
        ArrayList list = new ArrayList();
        ComponentHandler[] components = getComponentHandlers();
        if( components.length > 0 )
        {
            if( m_provider.getLogger().isTraceEnabled() )
            {
                m_provider.getLogger().trace( "commissioning internal parts" );
            }
            for( int i=0; i<components.length; i++ )
            {
                ComponentHandler component = components[i];
                try
                {
                    if( component.getActivationPolicy().equals( ActivationPolicy.STARTUP ) )
                    {
                        component.commission();
                    }
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Error during the commission of the internal parts of a component."
                      + "\nProvider: " + m_provider
                      + "\nPart: " + component;
                    throw new ControllerException( error, e );
                }
            }
        }
        m_commissioned = true;
    }
    
   /**
    * Initiate decommissioning of all internal parts.
    */
    public synchronized void decommission()
    {
        if( !m_commissioned )
        {
            return;
        }
        ComponentHandler[] components = getComponentHandlers();
        if( components.length > 0 )
        {
            if( m_provider.getLogger().isTraceEnabled() )
            {
                m_provider.getLogger().trace( "decommissioning internal parts" );
            }
            for( int i=0; i<components.length; i++ )
            {
                ComponentHandler component = components[i];
                try
                {
                    component.decommission();
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Error during the decommission of the internal parts of a component."
                      + "\nProvider: " + m_provider
                      + "\nPart: " + component;
                    m_provider.getLogger().warn( error, e );
                }
            }
        }
        m_commissioned = false;
    }

    
    private Classpath resolveClasspath( ComponentDirective directive )
    {
        if( null != directive.getBasePart() )
        {
            return directive.getBasePart().getClasspath();
        }
        else
        {
            return new Classpath();
        }
    }
}
