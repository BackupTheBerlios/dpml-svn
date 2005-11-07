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

package net.dpml.profile.impl;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.model.DefaultModel;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.Logger;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.RegistryEvent;
import net.dpml.profile.RegistryListener;
import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ApplicationProfile.StartupPolicy;

/**
 * Implements of the application registry within which a set of application profiles 
 * are maintained.
 */
public class RegistryModel extends DefaultModel implements ApplicationRegistry
{
    private final RegistryStorage m_home;

    private final List m_list = Collections.synchronizedList( new LinkedList() );

   /**
    * Creation of a new application registry model.
    * @param logger the assigned logging channel
    * @param store the storage unit
    * @exception NullPointerException if the supplied logging channel is null
    * @exception DuplicateKeyException if the store contains duplicate key defintions
    * @exception RemoteException if a remote error occurs
    */
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

   /**
    * Return the number of application profiles in the registry.
    * @return the application profile count
    */
    public int getApplicationProfileCount()
    {
        return m_list.size();
    }

   /**
    * Add an application profile to the registry.
    * @param profile the application profile to add to the registry
    * @exception DuplicateKeyException if the profile key is already assigned
    * @exception RemoteException if a remote error occurs
    */
    public void addApplicationProfile( ApplicationProfile profile ) 
      throws DuplicateKeyException, RemoteException
    {
        addApplicationProfile( profile, true );
    }

   /**
    * Remove an application profile from the registry.
    * @param profile the application profile to remove 
    * @exception RemoteException if a remote error occurs
    */
    public void removeApplicationProfile( ApplicationProfile profile ) throws RemoteException
    {
        synchronized( getLock() )
        {
            profile.dispose();
            m_list.remove( profile );
            ProfileRemovedEvent event = new ProfileRemovedEvent( this, profile );
            super.enqueueEvent( event );
        }
    }

   /**
    * Return an array of all profiles in the registry.
    * @return the application profiles
    */
    public ApplicationProfile[] getApplicationProfiles()
    {
        synchronized( getLock() )
        {
            return (ApplicationProfile[]) m_list.toArray( new ApplicationProfile[0] );
        }
    }

   /**
    * Create an return a new unnamed application profile.
    * @param codebase the application codebase uri
    * @return the application profile
    * @exception RemoteException if a transport error occurs
    */
    public ApplicationProfile createAnonymousApplicationProfile( URI codebase ) throws RemoteException
    {
        Logger logger = getLogger();
        String id = "anonymous";
        String title = id;
        Properties properties = new Properties();
        StartupPolicy policy = ApplicationProfile.MANUAL;
        Value[] params = new Value[0];
        String path = "${user.dir}";
        return new ApplicationModel(  
          logger, id, title, properties, path, codebase, policy, 
          ApplicationProfile.DEFAULT_STARTUP_TIMEOUT, 
          ApplicationProfile.DEFAULT_SHUTDOWN_TIMEOUT, 
          params );
    }

   /**
    * Retrieve an application profile.
    * @param key the application profile key
    * @return the application profile
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a transport error occurs
    */
    public ApplicationProfile getApplicationProfile( String key ) 
      throws UnknownKeyException, RemoteException
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
    public void addRegistryListener( RegistryListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Add a registry change listener.
    * @param listener the registry change listener to add
    */
    public void removeRegistryListener( RegistryListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Proces a registry event.
    * @param event the event top process
    */
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

   /**
    * Return a string representation of the registy model.
    * @return the string value
    */
    public String toString()
    {
        return "[registry]";
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
        synchronized( getLock() )
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

   /**
    * ProfileAddedEvent.
    */
    static class ProfileAddedEvent extends RegistryEvent
    {
       /**
        * Creation of a new ProfileAddedEvent.
        * @param source the source registry
        * @param profile the profile that was added
        */
        public ProfileAddedEvent( ApplicationRegistry source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }

   /**
    * ProfileRemovedEvent.
    */
    static class ProfileRemovedEvent extends RegistryEvent
    {
       /**
        * Creation of a new ProfileRemovedEvent.
        * @param source the source registry
        * @param profile the profile that was removed
        */
        public ProfileRemovedEvent( ApplicationRegistry source, ApplicationProfile profile )
        {
            super( source, profile );
        }
    }
}
