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
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.Logger;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.model.Value;

import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ApplicationProfileEvent;
import net.dpml.profile.ApplicationProfileListener;
import net.dpml.profile.ApplicationProfile.StartupPolicy;

/**
 * A DefaultApplicationProfile maintains information about the configuration
 * of an application profile.
 */
public class ApplicationModel extends DefaultContentModel implements ApplicationProfile
{
    private final ApplicationStorage m_store;

    private Properties m_properties;
    private StartupPolicy m_policy;
    private boolean m_server;
    private String m_path;
    private int m_startup;
    private int m_shutdown;

   /**
    * Creation of a new application model.
    * @param logger the logging channel
    * @param id the model id
    * @param title the model title
    * @param properties application properties
    * @param working the application working directory
    * @param uri the application codebase uri
    * @param policy the startup policy
    * @param startup the startup timeout value
    * @param shutdown the shutdown timeout value
    * @param params application instantiation parameters
    * @exception RemoteException if a transport error occurs
    */
    public ApplicationModel( 
      Logger logger, String id, String title, 
      Properties properties, String working, URI uri, 
      StartupPolicy policy, int startup, int shutdown, Value[] params ) 
      throws RemoteException
    {
        super( logger, id, uri, params, "app", title );

        m_store = null;
        m_properties = properties;
        m_policy = policy;
        m_path = working;
        m_startup = startup;
        m_shutdown = shutdown;
    }

   /**
    * Creation of a new application model.
    * @param logger the logging channel
    * @param store the storage unit
    * @exception RemoteException if a transport error occurs
    */
    public ApplicationModel( Logger logger, ApplicationStorage store )
      throws RemoteException
    {
        super( logger, store );

        m_store = store;
        m_policy = store.getStartupPolicy();
        m_properties = store.getSystemProperties();
        m_path = store.getWorkingDirectoryPath();
        m_startup = store.getStartupTimeout();
        m_shutdown = store.getShutdownTimeout();
    }

    //----------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------

   /**
    * Add a change listener.
    * @param listener the application profile change listener to add
    */
    public void addApplicationProfileListener( ApplicationProfileListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    public void removeApplicationProfileListener(  ApplicationProfileListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Return the system wide unique application identifier.
    *
    * @return the application identifier
    */
    public String getID()
    {
        return super.getID();
    }

   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    public int getStartupTimeout()
    {
        return m_startup;
    }

   /**
    * Set the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @param timeout the startup timeout value
    */
    public void setStartupTimeout( int timeout )
    {
        synchronized( getLock() )
        {
            m_startup = timeout;
            if( null != m_store )
            {
                m_store.setStartupTimeout( timeout );
            }
            StartupTimeoutChangedEvent event = new StartupTimeoutChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    public int getShutdownTimeout()
    {
        return m_shutdown;
    }

   /**
    * Set the duration in seconds to wait for shutdown
    * of the application before considering the application as non-responsive.
    * 
    * @param timeout the shutdown timeout value
    */
    public void setShutdownTimeout( int timeout )
    {
        synchronized( getLock() )
        {
            m_shutdown = timeout;
            if( null != m_store )
            {
                m_store.setShutdownTimeout( timeout );
            }
            ShutdownTimeoutChangedEvent event = new ShutdownTimeoutChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Get the working directory path.  The value returned may include
    * symbolic references to system properties in the form ${name} where 
    * 'name' corresponds to a system property name.
    * 
    * @return the working directory path
    */
    public String getWorkingDirectoryPath()
    {
        return m_path;
    }

   /**
    * Set the working directory path.  The value supplied may include
    * symbolic references to system properties in the form ${name} where 
    * 'name' corresponds to a system property name.
    * 
    * @param path the working directory path
    */
    public void setWorkingDirectoryPath( String path )
    {
        synchronized( getLock() )
        {
            m_path = path;
            if( null != m_store )
            {
                m_store.setWorkingDirectoryPath( path );
            }
            WorkingDirectoryChangedEvent event = new WorkingDirectoryChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Get the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @return the system properties set
    */
    public Properties getSystemProperties()
    {
        return m_properties;
    }

   /**
    * Set the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @param properties the system properties set
    */
    public void setSystemProperties( Properties properties )
    {
        synchronized( getLock() )
        {
            m_properties = properties;
            if( null != m_store )
            {
                m_store.setSystemProperties( properties );
            }
            SystemPropertiesChangedEvent event = new SystemPropertiesChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Set a system property to be assigned to a target virtual machine.
    * 
    * @param key the system property key
    * @param value the property value
    */
    public void setSystemProperty( String key, String value )
    {
        synchronized( getLock() )
        {
            m_properties.setProperty( key, value );
            if( null != m_store )
            {
                m_store.setSystemProperty( key, value );
            }
            SystemPropertiesChangedEvent event = new SystemPropertiesChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Return the startup policy for the application.  If the policy
    * is DISABLED the application cannot be started.  If the policy 
    * is MANUAL startup may be invoked manually.  If the policy is 
    * AUTOMATIC then startup will be handled by the Station.
    *
    * @return the startup policy
    */
    public StartupPolicy getStartupPolicy()
    {
        return m_policy;
    }

   /**
    * Set the the startup policy to one of DISABLED, MANUAL or AUTOMATIC.
    * @param policy the startup policy
    */
    public void setStartupPolicy( StartupPolicy policy )
    {
        synchronized( getLock() )
        {
            m_policy = policy;
            if( m_store != null )
            {
                m_store.setStartupPolicy( policy );
            }
            StartupPolicyChangedEvent event = new StartupPolicyChangedEvent( this );
            super.enqueueEvent( event );
        }
    }

   /**
    * Return the string representation of the application model.
    * @return the string value
    */
    public String toString()
    {
        try
        {
            return "[app:" + getID() + "]";
        }
        catch( Exception e )
        {
            return "[app:error]";
        }
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof ApplicationProfileEvent )
        {
            processApplicationProfileEvent( (ApplicationProfileEvent) event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + event.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processApplicationProfileEvent( ApplicationProfileEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ApplicationProfileListener )
            {
                ApplicationProfileListener rl = (ApplicationProfileListener) listener;
                try
                {
                    if( event instanceof TitleChangedEvent )
                    {
                        rl.titleChange( event );
                    }
                    else if( event instanceof WorkingDirectoryChangedEvent )
                    {
                        rl.workingDirectoryPathChanged( event );
                    }
                    else if( event instanceof SystemPropertiesChangedEvent )
                    {
                        rl.systemPropertiesChanged( event );
                    }
                    else if( event instanceof StartupTimeoutChangedEvent )
                    {
                        rl.startupTimeoutChanged( event );
                    }
                    else if( event instanceof ShutdownTimeoutChangedEvent )
                    {
                        rl.shutdownTimeoutChanged( event );
                    }
                    else if( event instanceof StartupPolicyChangedEvent )
                    {
                        rl.startupPolicyChanged( event );
                    }
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

    static class TitleChangedEvent extends ApplicationProfileEvent
    {
        public TitleChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }

    static class WorkingDirectoryChangedEvent extends ApplicationProfileEvent
    {
        public WorkingDirectoryChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }

    static class SystemPropertiesChangedEvent extends ApplicationProfileEvent
    {
        public SystemPropertiesChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }

    static class StartupTimeoutChangedEvent extends ApplicationProfileEvent
    {
        public StartupTimeoutChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }

    static class ShutdownTimeoutChangedEvent extends ApplicationProfileEvent
    {
        public ShutdownTimeoutChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }

    static class StartupPolicyChangedEvent extends ApplicationProfileEvent
    {
        public StartupPolicyChangedEvent( ApplicationProfile profile )
        {
            super( profile );
        }
    }
}


