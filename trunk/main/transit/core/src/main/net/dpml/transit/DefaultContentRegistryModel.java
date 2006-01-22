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

package net.dpml.transit;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ContentRegistryListener;
import net.dpml.transit.model.ContentRegistryEvent;

import net.dpml.lang.UnknownKeyException;
import net.dpml.lang.DuplicateKeyException;

/**
 * Default implementation of a content model registry manager that maitains 
 * information about the set of registred content models.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultContentRegistryModel extends DefaultModel 
  implements ContentRegistryModel, Disposable
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final List m_list = Collections.synchronizedList( new LinkedList() );

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new content registry model.
    * @param logger the supplied logging channel
    * @param home the content registry storage home
    * @exception DuplicateKeyException if the supplied home contains 
    *   duplicate content model identities
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultContentRegistryModel( Logger logger, ContentDirective[] handlers ) 
      throws DuplicateKeyException, RemoteException
    {
        super( logger );

        for( int i=0; i < handlers.length; i++ )
        {
            ContentDirective directive = handlers[i];
            String id = directive.getID();
            Logger log = logger.getChildLogger( id );
            ContentModel model = new DefaultContentModel( log, directive );
            addContentModel( model, false );
        }
    }

    // ------------------------------------------------------------------------
    // Disposable
    // ------------------------------------------------------------------------

   /**
    * Disposal of the model.
    * @exception RemoteException if a remote exception occurs
    */
    public synchronized void dispose()
    {
        ContentModel[] models = getContentModels();
        for( int i=0; i<models.length; i++ )
        {
            ContentModel model = models[i];
            dispose( model );
        }
        super.dispose();
    }

    private void dispose( Object object )
    {
        if( object instanceof Disposable )
        {
            Disposable disposable = (Disposable) object;
            disposable.dispose();
        }
        if( object instanceof Remote )
        {
            try
            {
                Remote remote = (Remote) object;
                UnicastRemoteObject.unexportObject( remote, true );
            }
            catch( RemoteException re )
            {
                getLogger().warn( "Unexpected error during remote reference removal.", re );
            }
        }
    }

    // ------------------------------------------------------------------------
    // ContentRegistryModel
    // ------------------------------------------------------------------------
    
   /**
    * Return an array of content models currently assigned to the registry.
    * @return the content model array
    */
    public ContentModel[] getContentModels()
    {
        synchronized( getLock() )
        {
            return (ContentModel[]) m_list.toArray( new ContentModel[0] );
        }
    }

   /**
    * Return a content model matching the supplied type.
    *
    * @param type the content model type
    * @return the content model
    * @exception UnknownKeyException if the content model type is unknown
    */
    public ContentModel getContentModel( String type ) throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            ContentModel[] managers = getContentModels();
            for( int i=0; i < managers.length; i++ )
            {
                ContentModel manager = managers[i];
                try
                {
                    if( type.equals( manager.getID() ) )
                    {
                        return manager;
                    }
                }
                catch( RemoteException e )
                {
                    throw new ModelRuntimeException( e.getMessage(), e );
                }
            }
            throw new UnknownKeyException( type );
        }
    }

   /**
    * Add a regsitry change listener.
    * @param listener the registry change listener to add
    */
    public void addRegistryListener( ContentRegistryListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a registry change listener.
    * @param listener the registry change listener to remove
    */
    public void removeRegistryListener( ContentRegistryListener listener )
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

    private void addContentModel( ContentModel manager, boolean notify ) 
      throws DuplicateKeyException
    {
        synchronized( getLock() )
        {
            try
            {
                String id = manager.getID();
                ContentModel m = getContentModel( id );
                throw new DuplicateKeyException( id );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( manager );
                if( notify )
                {
                    ContentAddedEvent event = new ContentAddedEvent( this, manager );
                    enqueueEvent( event );
                }
            }
            catch( RemoteException e )
            {
                throw new ModelRuntimeException( e.getMessage(), e );
            }
        }
    }

   /**
    * Internal event handler.
    * @param event the event to handle
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof ContentRegistryEvent )
        {
            processContentRegistryEvent( (ContentRegistryEvent) event );
        }
    }

    private void processContentRegistryEvent( ContentRegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
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

   /**
    * Content model addition event.
    */
    static class ContentAddedEvent extends ContentRegistryEvent
    {
       /**
        * Creation of a new content model addition event.
        * @param source the source registry
        * @param handler the content model
        */
        public ContentAddedEvent( ContentRegistryModel source, ContentModel handler )
        {
            super( source, handler );
        }
    }

   /**
    * Content model removal event.
    */
    static class ContentRemovedEvent extends ContentRegistryEvent
    {
       /**
        * Creation of a new content model removal event.
        * @param source the source registry
        * @param handler the content model
        */
        public ContentRemovedEvent( ContentRegistryModel source, ContentModel handler )
        {
            super( source, handler );
        }
    }
}


