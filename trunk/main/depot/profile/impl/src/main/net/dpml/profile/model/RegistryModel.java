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
import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.RegistryEvent;
import net.dpml.profile.RegistryListener;
import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.RegistryStorage;
import net.dpml.profile.ApplicationStorage;

/**
 * Implements of the DepotModel within which a set of application profiles 
 * are maintained.
 */
public class RegistryModel extends AbstractModel implements ApplicationRegistry
{
    private final RegistryStorage m_home;

    private final List m_list = Collections.synchronizedList( new LinkedList() );

    public RegistryModel( Logger logger, RegistryStorage store ) 
      throws NullPointerException, DuplicateKeyException, RemoteException
    {
         super( logger );

         m_home = store;

         ApplicationStorage[] applications = store.getInitialApplicationStorageArray();
         for( int i=0; i<applications.length; i++ )
         {
             ApplicationStorage application = applications[i];
             addApplicationProfile( application, false );
         }
    }

    public int getApplicationProfileCount() throws RemoteException
    {
        return m_list.size();
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

    public ApplicationProfile createAnonymousApplicationProfile( URI codebase ) throws RemoteException
    {
        Logger logger = getLogger();
        String id = "anonymous";
        String title = id;
        Properties properties = new Properties();
        boolean server = true;
        boolean enabled = false;
        Parameter[] params = new Parameter[0];
        return new ApplicationModel(  
          logger, id, title, properties, server, codebase, enabled, params );
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

   /**
    * Add a depot content change listener.
    * @param listener the registry change listener to add
    */
    public void addRegistryListener( RegistryListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    public void removeRegistryListener( RegistryListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof RegistryEvent )
        {
            processDepotEvent( (RegistryEvent) event );
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
        ApplicationProfile profile = new ApplicationModel( logger, store );
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

    private void processDepotEvent( RegistryEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof RegistryListener )
            {
                RegistryListener rl = (RegistryListener) listener;
                if( event instanceof ProfileAddedEvent )
                {
                    try
                    {
                        rl.profileAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "RegistryListener profile addition notification error.";
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
                          "RegistryListener profile removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    static class ProfileAddedEvent extends RegistryEvent
    {
        public ProfileAddedEvent( ApplicationRegistry source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }

    static class ProfileRemovedEvent extends RegistryEvent
    {
        public ProfileRemovedEvent( ApplicationRegistry source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }
}
