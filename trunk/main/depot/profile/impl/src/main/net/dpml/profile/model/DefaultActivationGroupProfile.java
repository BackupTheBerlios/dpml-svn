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

package net.dpml.profile.model;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationGroupDesc;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

import net.dpml.profile.ActivationProfile;
import net.dpml.profile.ActivationGroupProfile;
import net.dpml.profile.ActivationGroupListener;
import net.dpml.profile.ActivationGroupEvent;
import net.dpml.profile.store.ActivationStorage;
import net.dpml.profile.store.ActivationGroupStorage;

/**
 * A ActivationProfile maintains information about the configuration
 * of an activation profile.
 */
public class DefaultActivationGroupProfile extends DefaultProfile implements ActivationGroupProfile
{
    private final ActivationGroupStorage m_store;

    private final List m_list = Collections.synchronizedList( new LinkedList() );

    public DefaultActivationGroupProfile( Logger logger, ActivationGroupStorage home )
      throws RemoteException, DuplicateKeyException
    {
        super( logger, home );
        
        m_store = home;

        ActivationStorage[] stores = home.getInitialActivationStorageArray();
        for( int i=0; i<stores.length; i++ )
        {
            ActivationStorage store = stores[i];
            addActivationProfile( store, false );            
        }
    }

    public ActivationProfile[] getActivationProfiles()
    {
        synchronized( m_lock )
        {
            return (ActivationProfile[]) m_list.toArray( new ActivationProfile[0] );
        }
    }

    public ActivationProfile getActivationProfile( String key ) throws UnknownKeyException, RemoteException
    {
        ActivationProfile[] profiles = getActivationProfiles();
        for( int i=0; i<profiles.length; i++ )
        {
            ActivationProfile profile = profiles[i];
            if( key.equals( profile.getID() ) )
            {
                return profile;
            }
        }
        throw new UnknownKeyException( key );
    }

    public void addActivationProfile( ActivationProfile profile ) 
      throws DuplicateKeyException, RemoteException
    {
        addActivationProfile( profile, true );
    }

    public void removeActivationProfile( ActivationProfile profile ) throws RemoteException
    {
        synchronized( m_lock )
        {
            profile.dispose();
            m_list.remove( profile );
            ProfileRemovedEvent event = new ProfileRemovedEvent( this, profile );
            super.enqueueEvent( event );
        }
    }

   /**
    * Add a depot content change listener.
    * @param listener the registry change listener to add
    */
    public void addActivationGroupListener( ActivationGroupListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    public void removeActivationGroupListener( ActivationGroupListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof ActivationGroupEvent )
        {
            processActivationGroupEvent( (ActivationGroupEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processActivationGroupEvent( ActivationGroupEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ActivationGroupListener )
            {
                ActivationGroupListener rl = (ActivationGroupListener) listener;
                if( event instanceof ProfileAddedEvent )
                {
                    try
                    {
                        rl.profileAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ActivationGroupListener profile addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ProfileRemovedEvent )
                {
                    try
                    {
                        rl.profileRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ActivationGroupListener profile removal notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    private void addActivationProfile( ActivationProfile profile, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String key = profile.getID();
            try
            {
                ActivationProfile current = getActivationProfile( key );
                throw new DuplicateKeyException( key );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( profile );
                if( notify )
                {
                    ProfileAddedEvent event = new ProfileAddedEvent( this, profile );
                    enqueueEvent( event );
                }
            }
        }
    }

    private void addActivationProfile( ActivationStorage store, boolean notify )
      throws DuplicateKeyException, RemoteException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        ActivationProfile profile = new DefaultActivationProfile( logger, store );
        addActivationProfile( profile, notify );
    }

    static class ProfileAddedEvent extends ActivationGroupEvent
    {
        public ProfileAddedEvent( ActivationGroupProfile source, ActivationProfile profile )
        {
            super( source, profile );
        }
    }

    static class ProfileRemovedEvent extends ActivationGroupEvent
    {
        public ProfileRemovedEvent( ActivationGroupProfile source, ActivationProfile profile )
        {
            super( source, profile );
        }
    }

}
