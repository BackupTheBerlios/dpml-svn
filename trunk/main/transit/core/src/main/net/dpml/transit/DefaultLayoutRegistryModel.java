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

import java.rmi.RemoteException;
import java.util.List;
import java.util.LinkedList;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.Logger;
import net.dpml.transit.store.LayoutRegistryHome;
import net.dpml.transit.store.LayoutStorage;
import net.dpml.transit.model.*;

/**
 * Default implementation of a layout registry model that maitains 
 * information about the set of available layout models.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultLayoutRegistryModel extends DisposableCodeBaseModel 
  implements LayoutRegistryModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final List m_list = new LinkedList();

    private final LayoutRegistryHome m_home;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new layout registry model.
    * @param logger the assinged logging channel
    * @param home the layout registry persistent storage home
    * @exception DuplicateKeyException if a duplicate layout is declared in the assingned home
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultLayoutRegistryModel( Logger logger, LayoutRegistryHome home )
      throws DuplicateKeyException, RemoteException
    {
        super( logger, home );

        m_home = home;

        LayoutStorage[] stores = home.getInitialLayoutStores();
        for( int i=0; i < stores.length; i++ )
        {
            LayoutStorage store = stores[i];
            addLayoutModel( store, false );
        }
    }

    // ------------------------------------------------------------------------
    // LayoutRegistryModel
    // ------------------------------------------------------------------------

   /**
    * Add a layout registry change listener.
    * @param listener the registry change listener to add
    */
    public void addLayoutRegistryListener( LayoutRegistryListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a layout registry change listener.
    * @param listener the registry change listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeLayoutRegistryListener( LayoutRegistryListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Add a new layout model to the registry.
    * @param id the layout model identity
    * @exception DuplicateKeyException if a layout model of the same id already exists
    */
    public void addLayoutModel( String id ) throws DuplicateKeyException
    {
        LayoutStorage store = m_home.getLayoutStorage( id );
        addLayoutModel( store, true );
    }

   /**
    * Add a new layout model to the registry.
    * @param model the layout model
    * @exception DuplicateKeyException if a layout model of the same id already exists
    */
    public void addLayoutModel( LayoutModel model ) throws DuplicateKeyException
    {
        addLayoutModel( model, true );
    }

   /**
    * Return an array of content managers currently assigned to the registry.
    * @return the content manager array
    * @exception RemoteException if a remote exception occurs
    */
    public LayoutModel[] getLayoutModels()
    {
        synchronized( getLock() )
        {
            return (LayoutModel[]) m_list.toArray( new LayoutModel[0] );
        }
    }

   /**
    * Return a layout resolver model matching the supplied id. If the id is unknown
    * an implementation shall return a null value.
    *
    * @return the layout model
    */
    public LayoutModel getLayoutModel( String id ) throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            if( null == id )
            {
                throw new NullPointerException( "id" );
            }
            LayoutModel[] managers = getLayoutModels();
            for( int i=0; i < managers.length; i++ )
            {
                LayoutModel manager = managers[i];
                try
                {
                    if( id.equals( manager.getID() ) )
                    {
                        return manager;
                    }
                }
                catch( RemoteException e )
                {
                    throw new ModelRuntimeException( "remote-exception", e );
                }
            }
            throw new UnknownKeyException( id );
        }
    }

   /**
    * Remove a layout model from the registry.
    * @param model the layout model to be removed
    * @exception ModelReferenceException if the layout is in use
    */
    public void removeLayoutModel( LayoutModel model ) throws ModelReferenceException
    {
        synchronized( getLock() )
        {
            try
            {
                try
                {
                    model.dispose();
                }
                catch( RemoteException remote )
                {
                    boolean ignoreit = true;
                }
                m_list.remove( model );
                LayoutRemovedEvent event = new LayoutRemovedEvent( this, model );
                enqueueEvent( event );
            }
            catch( VetoDisposalException e ) 
            {
                throw new ModelReferenceException( this, model );
            }
            
        }
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    private void addLayoutModel( LayoutStorage store, boolean notify ) 
      throws DuplicateKeyException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        try
        {
            LayoutModel model = new DefaultLayoutModel( logger, store );
            addLayoutModel( model, notify );
        }
        catch( RemoteException e )
        {
            throw new ModelRuntimeException( "remote-exception", e );
        }
    }

    private void addLayoutModel( LayoutModel manager, boolean notify ) 
      throws DuplicateKeyException
    {
        synchronized( getLock() )
        {
            try
            {
                String id = manager.getID();
                LayoutModel m = getLayoutModel( id );
                throw new DuplicateKeyException( id );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( manager );
                if( notify )
                {
                    LayoutAddedEvent event = new LayoutAddedEvent( this, manager );
                    enqueueEvent( event );
                }
            }
            catch( RemoteException e )
            {
                throw new ModelRuntimeException( "remote-exception", e );
            }
        }
    }

   /**
    * Internal event handler.
    * @param event the event to handle
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof LayoutRegistryEvent )
        {
            processLayoutRegistryEvent( (LayoutRegistryEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processLayoutRegistryEvent( LayoutRegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof LayoutRegistryListener )
            {
                LayoutRegistryListener rl = (LayoutRegistryListener) listener;
                if( event instanceof LayoutAddedEvent )
                {
                    try
                    {
                        rl.layoutAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "LayoutRegistryListener locator addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof LayoutRemovedEvent )
                {
                    try
                    {
                        rl.layoutRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "LayoutRegistryListener locator removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

   /**
    * Layout addition event.
    */
    static class LayoutAddedEvent extends LayoutRegistryEvent
    {
       /** 
        * Creation of a new layout model addition event.
        * @param source the layout registry
        * @param handler the layout model that was added
        */
        public LayoutAddedEvent( LayoutRegistryModel source, LayoutModel handler )
        {
            super( source, handler );
        }
    }

   /**
    * Layout removal event.
    */
    static class LayoutRemovedEvent extends LayoutRegistryEvent
    {
       /** 
        * Creation of a new layout model removal event.
        * @param source the layout registry
        * @param handler the layout model that was removed
        */
        public LayoutRemovedEvent( LayoutRegistryModel source, LayoutModel handler )
        {
            super( source, handler );
        }
    }
}


