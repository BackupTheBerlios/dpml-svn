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
import java.util.Properties;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.UnknownKeyException;

import net.dpml.profile.*;
import net.dpml.profile.DepotProfile;
import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ActivationGroupProfile;
import net.dpml.profile.DepotListener;
import net.dpml.profile.store.DepotHome;
import net.dpml.profile.store.ApplicationStorage;
import net.dpml.profile.store.ActivationGroupStorage;

/**
 * Implements of the DepotModel within which a set of application profiles 
 * are maintained.
 */
public class DefaultDepotProfile extends DefaultModel implements DepotProfile
{
    private final DepotHome m_home;

    private final List m_list = Collections.synchronizedList( new LinkedList() );
    private final List m_groups = Collections.synchronizedList( new LinkedList() );

    public DefaultDepotProfile( Logger logger, DepotHome home ) 
      throws NullPointerException, DuplicateKeyException, RemoteException
    {
         super( logger );

         m_home = home;

         ApplicationStorage[] stores = home.getInitialApplicationStorageArray();
         for( int i=0; i<stores.length; i++ )
         {
             ApplicationStorage store = stores[i];
             addApplicationProfile( store, false );
         }

         ActivationGroupStorage[] groups = home.getInitialActivationGroupStorageArray();
         for( int i=0; i<groups.length; i++ )
         {
             ActivationGroupStorage group = groups[i];
             addActivationGroupProfile( group, false );
         }
    }

    public int getApplicationProfileCount() throws RemoteException
    {
        return m_list.size();
    }

    public int getActivationGroupProfileCount() throws RemoteException
    {
        return m_groups.size();
    }

    public void addApplicationProfile( ApplicationProfile profile ) 
      throws DuplicateKeyException, RemoteException
    {
        addApplicationProfile( profile, true );
    }

    public void removeApplicationProfile( ApplicationProfile profile ) throws RemoteException
    {
        synchronized( m_lock )
        {
            profile.dispose();
            m_list.remove( profile );
            ProfileRemovedEvent event = new ProfileRemovedEvent( this, profile );
            super.enqueueEvent( event );
        }
    }

    public ApplicationProfile[] getApplicationProfiles() throws RemoteException
    {
        synchronized( m_lock )
        {
            return (ApplicationProfile[]) m_list.toArray( new ApplicationProfile[0] );
        }
    }

    public ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException
    {
        ApplicationProfile[] profiles = getApplicationProfiles();
        for( int i=0; i<profiles.length; i++ )
        {
            ApplicationProfile profile = profiles[i];
            if( key.equals( profile.getID() ) )
            {
                return profile;
            }
        }
        throw new UnknownKeyException( key );
    }

    public void addActivationGroupProfile( ActivationGroupProfile profile ) 
      throws DuplicateKeyException, RemoteException
    {
        addActivationGroupProfile( profile, true );
    }

    public void removeActivationGroupProfile( ActivationGroupProfile profile ) throws RemoteException
    {
        synchronized( m_lock )
        {
            profile.dispose();
            m_groups.remove( profile );
            GroupRemovedEvent event = new GroupRemovedEvent( this, profile );
            super.enqueueEvent( event );
        }
    }

    public ActivationGroupProfile[] getActivationGroupProfiles() throws RemoteException
    {
        synchronized( m_lock )
        {
            return (ActivationGroupProfile[]) m_groups.toArray( new ActivationGroupProfile[0] );
        }
    }

    public ActivationGroupProfile getActivationGroupProfile( String key )
      throws UnknownKeyException, RemoteException
    {
        ActivationGroupProfile[] profiles = getActivationGroupProfiles();
        for( int i=0; i<profiles.length; i++ )
        {
            ActivationGroupProfile profile = profiles[i];
            if( key.equals( profile.getID() ) )
            {
                return profile;
            }
        }
        throw new UnknownKeyException( key );
    }


   /**
    * Add a depot content change listener.
    * @param listener the registry change listener to add
    */
    public void addDepotListener( DepotListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    public void removeDepotListener( DepotListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof DepotApplicationEvent )
        {
            processDepotEvent( (DepotEvent) event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + event.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void addApplicationProfile( ApplicationStorage store, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        ApplicationProfile profile = new DefaultApplicationProfile( logger, store );
        addApplicationProfile( profile, notify );
    }

    private void addApplicationProfile( ApplicationProfile profile, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String key = profile.getID();
            try
            {
                ApplicationProfile current = getApplicationProfile( key );
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

    private void addActivationGroupProfile( ActivationGroupStorage store, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        ActivationGroupProfile profile = new DefaultActivationGroupProfile( logger, store );
        addActivationGroupProfile( profile, notify );
    }

    private void addActivationGroupProfile( ActivationGroupProfile profile, boolean notify ) 
      throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String key = profile.getID();
            try
            {
                ActivationGroupProfile current = getActivationGroupProfile( key );
                throw new DuplicateKeyException( key );
            }
            catch( UnknownKeyException e )
            {
                m_groups.add( profile );
                if( notify )
                {
                    GroupAddedEvent event = new GroupAddedEvent( this, profile );
                    enqueueEvent( event );
                }
            }
        }
    }

    private void processDepotEvent( DepotEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof DepotListener )
            {
                DepotListener rl = (DepotListener) listener;
                if( event instanceof ProfileAddedEvent )
                {
                    try
                    {
                        rl.profileAdded( (DepotApplicationEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "DepotListener profile addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof ProfileRemovedEvent )
                {
                    try
                    {
                        rl.profileRemoved( (DepotApplicationEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "DepotListener profile removed notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof GroupAddedEvent )
                {
                    try
                    {
                        rl.groupAdded( (DepotGroupEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "DepotListener group addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof GroupRemovedEvent )
                {
                    try
                    {
                        rl.groupRemoved( (DepotGroupEvent) event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "DepotListener group removal notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    static class ProfileAddedEvent extends DepotApplicationEvent
    {
        public ProfileAddedEvent( DepotProfile source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }

    static class ProfileRemovedEvent extends DepotApplicationEvent
    {
        public ProfileRemovedEvent( DepotProfile source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }

    static class GroupAddedEvent extends DepotGroupEvent
    {
        public GroupAddedEvent( DepotProfile source, ActivationGroupProfile profile )
        {
            super( source, profile );
        }
    }

    static class GroupRemovedEvent extends DepotGroupEvent
    {
        public GroupRemovedEvent( DepotProfile source, ActivationGroupProfile profile )
        {
            super( source, profile );
        }
    }

}
