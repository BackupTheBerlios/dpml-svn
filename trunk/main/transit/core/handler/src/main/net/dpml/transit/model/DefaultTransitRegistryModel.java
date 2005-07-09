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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.EventObject;
import java.util.EventListener;
import java.rmi.RemoteException;

import net.dpml.transit.TransitError;
import net.dpml.transit.store.TransitStorage;
import net.dpml.transit.store.TransitHome;
import net.dpml.transit.unit.TransitStorageUnit;

/**
 * The DefaultTransitRegistryModel class maintains the set of 
 * registered Transit configurations.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTransitRegistryModel extends DefaultModel implements TransitRegistryModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final TransitHome m_home;

    private final List m_list = Collections.synchronizedList( new LinkedList() );

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultTransitRegistryModel( Logger logger, TransitHome home ) 
      throws RemoteException, DuplicateKeyException
    {
        super( logger );

        m_home = home;

        TransitStorage[] stores = m_home.getInitialTransitStores();
        for( int i=0; i<stores.length; i++ )
        {
            TransitStorage store = stores[i];
            addTransitModel( store, false );
        }
    }

    // ------------------------------------------------------------------------
    // TransitRegistryModel
    // ------------------------------------------------------------------------

    public int getTransitModelCount()
    {
        synchronized( m_lock )
        {
            return m_list.size();
        }
    }

   /**
    * Add a regstry change listener.
    * @param listener the registry change listener to add
    */
    public void addTransitRegistryListener( TransitRegistryListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a regstry change listener.
    * @param listener the registry change listener to remove
    */
    public void removeTransitRegistryListener( TransitRegistryListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    public void addTransitModel( String id ) throws DuplicateKeyException, RemoteException
    {
        TransitStorage store = m_home.getTransitStorage( id );
        addTransitModel( store, true );
    }

    public void addTransitModel( TransitModel model ) throws DuplicateKeyException, RemoteException
    {
        addTransitModel( model, true );
    }

    private void addTransitModel( TransitStorage store, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        TransitModel model = new DefaultTransitModel( logger, store );
        addTransitModel( model, notify );
    }

    private void addTransitModel( TransitModel model, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String id = model.getID();
            try
            {
                TransitModel m = getTransitModel( id );
                throw new DuplicateKeyException( id );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( model );
                if( notify )
                {
                    ModelAddedEvent event = new ModelAddedEvent( this, model );
                    enqueueEvent( event );
                }
            }
        }
    }

    public TransitModel[] getTransitModels() throws RemoteException
    {
        synchronized( m_lock )
        {
            return (TransitModel[]) m_list.toArray( new TransitModel[0] );
        }
    }

    public TransitModel getTransitModel( String id ) throws UnknownKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            if( null == id )
            {
                throw new NullPointerException( "id" );
            }
            TransitModel[] models = getTransitModels();
            for( int i=0; i<models.length; i++ )
            {
                TransitModel model = models[i];
                if( id.equals( model.getID() ) )
                {
                    return model;
                }
            }
            throw new UnknownKeyException( id );
        }
    }

    public void removeTransitModel( TransitModel model ) throws ModelReferenceException, RemoteException
    {
        synchronized( m_lock )
        {
            try
            {
                model.dispose();
                m_list.remove( model );
                ModelRemovedEvent event = new ModelRemovedEvent( this, model );
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

    public void processEvent( EventObject event )
    {
        if( event instanceof TransitRegistryEvent )
        {
            processTransitRegistryEvent( (TransitRegistryEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processTransitRegistryEvent( TransitRegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof TransitRegistryListener )
            {
                TransitRegistryListener rl = (TransitRegistryListener) listener;
                if( event instanceof ModelAddedEvent )
                {
                    try
                    {
                        rl.modelAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "TransitRegistryListener model addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ModelRemovedEvent )
                {
                    try
                    {
                        rl.modelRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "TransitRegistryListener model removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    static class ModelAddedEvent extends TransitRegistryEvent
    {
        public ModelAddedEvent( TransitRegistryModel source, TransitModel handler )
        {
            super( source, handler );
        }
    }

    static class ModelRemovedEvent extends TransitRegistryEvent
    {
        public ModelRemovedEvent( TransitRegistryModel source, TransitModel handler )
        {
            super( source, handler );
        }
    }
}

