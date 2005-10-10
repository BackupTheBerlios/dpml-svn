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

package net.dpml.transit;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.LayoutRegistryListener;
import net.dpml.transit.model.LayoutRegistryEvent;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.Logger;

/**
 * A registry of descriptions of plugable layout models.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class DefaultLayoutRegistry extends UnicastRemoteObject implements LayoutRegistry, LayoutRegistryListener
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;

    private final Map m_plugins = new WeakHashMap();

    private final LayoutRegistryModel m_model;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Provides support for the resolution of location resolver instances.
    */
    public DefaultLayoutRegistry( LayoutRegistryModel model, Logger logger ) throws RemoteException
    {
        super();

        m_model = model;
        m_logger = logger;
        model.addLayoutRegistryListener( this );
    }

    // ------------------------------------------------------------------------
    // LayoutRegistry
    // ------------------------------------------------------------------------

    public Layout getLayout( final String id ) throws IOException
    {
        LayoutModel model = m_model.getLayoutModel( id );
        return getLayout( model );
    }

   /**
    * Locate and return a location resolver.
    * @param model the location resolver model
    * @return the resolver
    */
    protected Layout getLayout( LayoutModel model ) throws IOException
    {
        String id = model.getID();
        String classname = model.getClassname();
        if( null != classname )
        {
            return loadLayout( classname );
        }
        else
        {
            return loadLayout( model );
        }
    }

    protected Layout loadLayout( final String classname ) throws TransitException
    {
        if( ClassicLayout.class.getName().equals( classname ) )
        {
            return CLASSIC_RESOLVER;
        }
        else if( EclipseLayout.class.getName().equals( classname ) )
        {
            return ECLIPSE_RESOLVER;
        }
        else
        {
            final String error = 
              "Bootstrap location resolver classname not recognized."
              + "\nClassname: " + classname;
            throw new TransitException( error );
        }
    }

    protected Layout loadLayout( LayoutModel model ) throws IOException
    {
        Class clazz = loadLayoutClass( model );
        Repository loader = Transit.getInstance().getRepository();
        try
        {
            return (Layout) loader.instantiate( clazz, new Object[]{model} );
        }
        catch( Throwable e )
        { 
            final String error =  
              "Unable to load request model due to an instantiation failure."
              + "\nLayout ID: " + model.getID();
            throw new TransitException( error, e );
        }
    }

    protected Class loadLayoutClass( LayoutModel model ) throws IOException
    {
        URI uri = model.getCodeBaseURI();
        Class clazz = (Class) m_plugins.get( uri );
        if( null != clazz )
        {
            return clazz;
        }
        else
        {
            try
            {
                m_logger.debug( "loading resolver plugin: " + uri );
                Repository loader = Transit.getInstance().getRepository();
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                clazz = loader.getPluginClass( classloader, uri );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to load a layout plugin due to an unexpected exception.";
                throw new TransitException( error, e );
            }

            if( Layout.class.isAssignableFrom( clazz ) )
            {
                m_plugins.put( uri, clazz );
                return clazz;
            }
            else
            {
                final String error = 
                  "Plugin is not assignable to a net.dpml.transit.location.Layout."
                  + "\nPlugin URI: " + uri 
                  + "\nPlugin Class: " + clazz.getName();
                throw new TransitException( error );
            }
        }
    }

    // ------------------------------------------------------------------------
    // LayoutRegistryListener
    // ------------------------------------------------------------------------

   /**
    * Notify all listeners of the addition of a layout model.
    * @param event the registry event
    */
    public void layoutAdded( LayoutRegistryEvent event ) throws RemoteException
    {
    }

   /**
    * Notify all listeners of the removal of a content model.
    * @param event the registry event
    */
    public void layoutRemoved( LayoutRegistryEvent event ) throws RemoteException
    {
    }

    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------
    
    private static final Layout CLASSIC_RESOLVER = new ClassicLayout();
    private static final Layout ECLIPSE_RESOLVER = new EclipseLayout();
}

