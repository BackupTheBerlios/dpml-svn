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

import net.dpml.transit.Logger;
import net.dpml.transit.store.TransitStorage;
import net.dpml.transit.store.TransitHome;

/**
 * The DefaultTransitRegistryModel class maintains the set of 
 * registered Transit profiles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
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

   /**
    * Creation of a new Transit registry.
    * @param logger the assigned logging channel
    * @param home the registry storage unit
    * @exception DuplicateKeyException if a suplicate is present in the subsystems
    *   declared within the supplied storage unit
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitRegistryModel( Logger logger, TransitHome home ) 
      throws RemoteException, DuplicateKeyException
    {
        super( logger );

        m_home = home;

        TransitStorage[] stores = m_home.getInitialTransitStores();
        for( int i=0; i < stores.length; i++ )
        {
            TransitStorage store = stores[i];
            addTransitModel( store, false );
        }
    }

    // ------------------------------------------------------------------------
    // TransitRegistryModel
    // ------------------------------------------------------------------------

   /**
    * Return the number of transit profiles within the registry.
    * @return the profile count
    */
    public int getTransitModelCount()
    {
        synchronized( getLock() )
        {
            return m_list.size();
        }
    }

   /**
    * Add a regstry change listener.
    * @param listener the registry change listener to add
    */
    public void addTransitRegistryListener( TransitRegistryListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a regstry change listener.
    * @param listener the registry change listener to remove
    */
    public void removeTransitRegistryListener( TransitRegistryListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Add a new Transit profile to the registry.
    * @param id the identifier of the new profile
    * @exception DuplicateKeyException if a profile with the same id is already registered
    */
    public void addTransitModel( String id ) throws DuplicateKeyException
    {
        TransitStorage store = m_home.getTransitStorage( id );
        addTransitModel( store, true );
    }

   /**
    * Add a new Transit profile to the registry.
    * @param model the profile to add
    * @exception DuplicateKeyException if a profile with the same id as the 
    *    id declared by the supplied model is already registered
    */
    public void addTransitModel( TransitModel model ) throws DuplicateKeyException
    {
        addTransitModel( model, true );
    }

   /**
    * Add a transit profile using a suppled stroage unit.
    * @param store the transit profile storage unit
    * @param notify TRUE if a notification event should be raised
    * @exception DuplicateKeyException if a profile with the same id as the 
    *    id declared by the supplied store is already registered
    */
    private void addTransitModel( TransitStorage store, boolean notify ) 
      throws DuplicateKeyException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        try
        {
            TransitModel model = new DefaultTransitModel( logger, store );
            addTransitModel( model, notify );
        }
        catch( RemoteException e )
        {
            throw new ModelRuntimeException( "remote-exception", e );
        }
    }

   /**
    * Add a transit profile using a supplied model.
    * @param model the transit profile
    * @param notify TRUE if a notification event should be raised
    * @exception DuplicateKeyException if a profile with the same id as the 
    *    id declared by the supplied model is already registered
    */
    private void addTransitModel( TransitModel model, boolean notify ) 
      throws DuplicateKeyException
    {
        synchronized( getLock() )
        {
            try
            {
                String id = model.getID();
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
            catch( RemoteException e )
            {
                throw new ModelRuntimeException( "remote-exception", e );
            }
        }
    }

   /**
    * Return the set of transit models in the registry.
    * @return the model array
    */
    public TransitModel[] getTransitModels()
    {
        synchronized( getLock() )
        {
            return (TransitModel[]) m_list.toArray( new TransitModel[0] );
        }
    }

   /**
    * Return a transit profile matching the supplied model identifier.
    * @param id the model identifier
    * @return the transit model
    * @exception UnknownKeyException if the key is not recognized
    */
    public TransitModel getTransitModel( String id ) throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            if( null == id )
            {
                throw new NullPointerException( "id" );
            }
            TransitModel[] models = getTransitModels();
            for( int i=0; i < models.length; i++ )
            {
                TransitModel model = models[i];
                try
                {
                    if( id.equals( model.getID() ) )
                    {
                        return model;
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
    * Remove a transit model from the registry.
    * @param model the model to remove
    * @exception ModelReferenceException if the model is in use
    */
    public void removeTransitModel( TransitModel model ) throws ModelReferenceException
    {
        synchronized( getLock() )
        {
            try
            {
                try
                {
                    model.dispose();
                }
                catch( RemoteException e )
                {
                    // ignore
                }
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

   /**
    * Internal event handler.
    * @param event the event to handle
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof TransitRegistryEvent )
        {
            processTransitRegistryEvent( (TransitRegistryEvent) event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + event.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processTransitRegistryEvent( TransitRegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
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

   /**
    * TransitModel addition event.
    */
    static class ModelAddedEvent extends TransitRegistryEvent
    {
       /**
        * Creation of a new model addition event.
        * @param source the transit model registry
        * @param handler the transit model that was added to the registry
        */
        public ModelAddedEvent( TransitRegistryModel source, TransitModel handler )
        {
            super( source, handler );
        }
    }

   /**
    * TransitModel removal event.
    */
    static class ModelRemovedEvent extends TransitRegistryEvent
    {
       /**
        * Creation of a new model removal event.
        * @param source the transit model registry
        * @param handler the transit model that was removed from the registry
        */
        public ModelRemovedEvent( TransitRegistryModel source, TransitModel handler )
        {
            super( source, handler );
        }
    }
}

