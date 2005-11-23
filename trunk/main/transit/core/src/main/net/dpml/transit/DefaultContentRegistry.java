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
import java.net.ContentHandler;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.Construct;
import net.dpml.transit.Value;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ContentRegistryListener;
import net.dpml.transit.model.ContentRegistryEvent;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.UnknownKeyException;

/**
 * A registry of descriptions of plugable content handlers. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultContentRegistry extends UnicastRemoteObject implements Service, ContentRegistry, ContentRegistryListener
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;

    private final Map m_plugins = new WeakHashMap();

    private final Map m_links = new WeakHashMap();

    private final Map m_handlers = new Hashtable();

    private final ContentRegistryModel m_model;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Responsible for the establishment of a map containing keys that correspond to 
    * artifact types, and values that correspond to the description of a 
    * content type handler plugins.
    */
    public DefaultContentRegistry( ContentRegistryModel model, Logger logger ) throws RemoteException
    {
        super();

        m_model = model;
        m_logger = logger;
        model.addRegistryListener( this );
    }

    // ------------------------------------------------------------------------
    // Handler
    // ------------------------------------------------------------------------

   /**
    * Dispose of the manager.  During disposal a manager is required to 
    * release all references such as listeners and internal resources
    * in preparation for garbage collection.
    */
    public void dispose()
    {
        try
        {
            m_model.removeRegistryListener( this );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while disposing of content registry.";
            m_logger.error( error, e );
        }
    }

    // ------------------------------------------------------------------------
    // ContentRegistry
    // ------------------------------------------------------------------------

    public ContentHandler getContentHandler( final String type ) throws IOException
    {
        try
        {
            ContentModel model = m_model.getContentModel( type );
            if( null != model.getCodeBaseURI() )
            {
                return getContentHandler( model );
            }
            else
            {
                return null;
            }
        }
        catch( UnknownKeyException e )
        {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // RegistryListener
    // ------------------------------------------------------------------------

   /**
    * Notify all listeners of the addition of a content model.
    * @param event the registry event
    */
    public void contentAdded( ContentRegistryEvent event ) throws RemoteException
    {
        synchronized( m_handlers )
        {
            ContentModel model = event.getContentModel();
            if( null != model.getCodeBaseURI() )
            {
                try
                {
                    getContentHandler( model ); // setup cached reference
                }
                catch( Exception e )
                {
                     final String error = 
                       "Internal error while attempting to add a content handler."
                       + "\nContent Type: " + model.getContentType()
                       + "\nPlugin URI: " + model.getCodeBaseURI();
                     m_logger.error( error, e );
                }
            }
        }
    }

   /**
    * Notify all listeners of the removal of a content model.
    * @param event the registry event
    */
    public void contentRemoved( ContentRegistryEvent event ) throws RemoteException
    {
        synchronized( m_handlers )
        {
            ContentModel model = event.getContentModel();
            try
            {
                 m_handlers.remove( model );
            }
            catch( Exception e )
            {
                 final String error = 
                   "Internal error while attempting to remove a content handler."
                   + "\nContent Type: " + model.getContentType()
                   + "\nPlugin URI: " + model.getCodeBaseURI();
                 m_logger.error( error, e );
            }
        }
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    protected ContentHandler getContentHandler( ContentModel model ) throws IOException
    {
        synchronized( m_handlers )
        {
            ContentHandler handler = (ContentHandler) m_handlers.get( model );
            if( null == handler )
            {
                Class clazz = loadContentHandlerClass( model );
                Repository loader = Transit.getInstance().getRepository();
                String type =  model.getContentType();
                Logger logger = getLogger().getChildLogger( type );
                Value[] params = model.getParameters();
                Map map = new Hashtable();
                map.put( "dpml.transit.logger", logger );
                map.put( "dpml.transit.content.model", model );
                try
                {
                    Object[] args = Construct.getArgs( map, params, new Object[]{logger, model} );
                    handler = (ContentHandler) loader.instantiate( clazz, args );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Unable to load content handler due to an instantiation failure."
                      + "\nContent Type: " + model.getContentType();
                    throw new TransitException( error, e );
                }
                m_handlers.put( model, handler );
            }
            return handler;
        }
    }

    protected Class loadContentHandlerClass( ContentModel model ) throws IOException
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
                m_logger.debug( "loading content handler plugin: " + uri );
                Repository loader = Transit.getInstance().getRepository();
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = loader.getPluginClass( system, uri );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to load a content handler plugin due to an unexpected exception.";
                throw new TransitException( error, e );
            }

            if( ContentHandler.class.isAssignableFrom( clazz ) )
            {
                m_plugins.put( uri, clazz );
                return clazz;
            }
            else
            {
                final String error = 
                  "Plugin is not assignable to a java.net.ContentHandler."
                  + "\nPlugin URI: " + uri 
                  + "\nPlugin Class: " + clazz.getName();
                throw new TransitException( error );
            }
        }
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
}

