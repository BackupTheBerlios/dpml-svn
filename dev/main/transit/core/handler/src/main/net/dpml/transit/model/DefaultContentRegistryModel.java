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

package net.dpml.transit.model;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.store.ContentStorage;
import net.dpml.transit.store.ContentRegistryHome;

/**
 * Default implementation of a content handler registry manager that maitains 
 * infomration about the set of registred plugin handler configurations.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: DefaultContentRegistryModel.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public class DefaultContentRegistryModel extends DisposableCodeBaseModel 
  implements ContentRegistryModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final List m_list = Collections.synchronizedList( new LinkedList() );

    private ContentRegistryHome m_home;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultContentRegistryModel( Logger logger, ContentRegistryHome home ) 
      throws DuplicateKeyException, RemoteException
    {
        super( logger, home );

        m_home = home;

        ContentStorage[] stores = home.getInitialContentStores();
        for( int i=0; i<stores.length; i++ )
        {
            ContentStorage store = stores[i];
            String id = store.getType();
            Logger log = logger.getChildLogger( id );
            ContentModel model = new DefaultContentModel( log, store );
            addContentModel( model, false );
        }
    }

    // ------------------------------------------------------------------------
    // ContentRegistryModel
    // ------------------------------------------------------------------------

   /**
    * Return an array of content managers currently assigned to the registry.
    * @return the content manager array
    */
    public ContentModel[] getContentModels() throws RemoteException
    {
        synchronized( m_lock )
        {
            return (ContentModel[]) m_list.toArray( new ContentModel[0] );
        }
    }

    public ContentModel getContentModel( String type ) throws UnknownKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            ContentModel[] managers = getContentModels();
            for( int i=0; i<managers.length; i++ )
            {
                ContentModel manager = managers[i];
                if( type.equals( manager.getContentType() ) )
                {
                    return manager;
                }
            }
            throw new UnknownKeyException( type );
        }
    }

    public void addContentModel( String type ) throws DuplicateKeyException, RemoteException
    {
        ContentStorage store = m_home.createContentStorage( type );
        Logger logger = getLogger().getChildLogger( type );
        DefaultContentModel model = new DefaultContentModel( logger, store );
        addContentModel( model );
    }

    public void addContentModel( String type, String title, URI uri ) 
      throws DuplicateKeyException, RemoteException
    {
        ContentStorage store = m_home.createContentStorage( type, title, uri );
        Logger logger = getLogger().getChildLogger( type );
        DefaultContentModel model = new DefaultContentModel( logger, store );
        addContentModel( model );
    }

    public void addContentModel( ContentModel manager ) 
      throws DuplicateKeyException, RemoteException
    {
        addContentModel( manager, true );
    }

    private void addContentModel( ContentModel manager, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String id = manager.getContentType();
            try
            {
                ContentModel m = getContentModel( id );
                throw new DuplicateKeyException( id );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( manager );
                if( notify )
                {
                    ContentAddedEvent event = new ContentAddedEvent ( this, manager );
                    enqueueEvent( event );
                }
            }
        }
    }

    public void removeContentModel( ContentModel model ) throws RemoteException
    {
        synchronized( m_lock )
        {
            model.dispose();
            m_list.remove( model );
            ContentRemovedEvent event = new ContentRemovedEvent( this, model );
            super.enqueueEvent( event );
        }
    }

   /**
    * Add a regstry change listener.
    * @param listener the registry change listener to add
    */
    public void addRegistryListener( ContentRegistryListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a regstry change listener.
    * @param listener the registry change listener to remove
    */
    public void removeRegistryListener( ContentRegistryListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

    public void processEvent( EventObject event )
    {
        if( event instanceof ContentRegistryEvent )
        {
            processContentRegistryEvent( (ContentRegistryEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processContentRegistryEvent( ContentRegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ContentRegistryListener )
            {
                ContentRegistryListener rl = (ContentRegistryListener) listener;
                if( event instanceof ContentAddedEvent )
                {
                    try
                    {
                        rl.contentAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ContentRegistryListener content addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ContentRemovedEvent )
                {
                    try
                    {
                        rl.contentRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ContentRegistryListener content removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    static class ContentAddedEvent extends ContentRegistryEvent
    {
        public ContentAddedEvent( ContentRegistryModel source, ContentModel handler )
        {
            super( source, handler );
        }
    }

    static class ContentRemovedEvent extends ContentRegistryEvent
    {
        public ContentRemovedEvent( ContentRegistryModel source, ContentModel handler )
        {
            super( source, handler );
        }
    }
}


