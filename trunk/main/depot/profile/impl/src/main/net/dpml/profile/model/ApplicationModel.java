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
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.model.Logger;
import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.DisposalListener;
import net.dpml.transit.model.DisposalEvent;
import net.dpml.transit.model.CodeBaseListener;
import net.dpml.transit.model.CodeBaseEvent;

import net.dpml.profile.Parameter;
import net.dpml.profile.ApplicationProfile;

/**
 * A DefaultApplicationProfile maintains information about the configuration
 * of an application profile.
 */
public class ApplicationModel extends AbstractModel implements ApplicationProfile
{
    private final ApplicationStorage m_store;
    private final String m_id;

    private String m_title;
    private Properties m_properties;
    private boolean m_enabled;
    private boolean m_server;
    private Parameter[] m_params;
    private URI m_uri;

    public ApplicationModel( 
      Logger logger, String id, String title, 
      Properties properties, boolean server, URI uri, 
      boolean enabled, Parameter[] params ) 
      throws RemoteException
    {
        super( logger );

        m_store = null;
        m_uri = uri;
        m_enabled = enabled;
        m_server = server;
        m_id = id;
        m_title = title;
        m_properties = properties;

        if( null == params )
        {
            m_params = new Parameter[0];
        }
        else
        {
            m_params = params;
        }
    }

    public ApplicationModel( Logger logger, ApplicationStorage store )
      throws RemoteException
    {
        super( logger );

        m_store = store;
        m_enabled = store.getEnabled();
        m_server = store.isaServer();
        m_params = store.getParameters();
        m_id = store.getID();
        m_title = store.getTitle();
        m_properties = store.getSystemProperties();
        m_uri = store.getCodeBaseURI();
    }

    //----------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------

    public String getID() throws RemoteException
    {
        return m_id;
    }

    public String getTitle() throws RemoteException
    {
        return m_title;
    }

    public void setTitle( String title ) throws RemoteException
    {
        m_title = title;
    }

    public Properties getSystemProperties() throws RemoteException
    {
        return m_properties;
    }

    public void setSystemProperties( Properties properties ) throws RemoteException
    {
        m_properties = properties;
    }

    public boolean isEnabled() throws RemoteException
    {
        return m_enabled;
    }

    public void setEnabled( boolean value ) throws RemoteException
    {
        m_enabled = value;
    }

    public boolean isaServer() throws RemoteException
    {
        return m_server;
    }

    public void setServerMode( boolean policy ) throws RemoteException
    {
        m_server = policy;
    }

    public Parameter[] getParameters() throws RemoteException
    {
        return m_params;
    }

    public void setParameters( Parameter[] params ) throws RemoteException
    {
        m_params = params;
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Add a disposal listener to the model.
    * @param listener the listener to add
    */
    public void addDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a disposal listener from the model.
    * @param listener the listener to remove
    */
    public void removeDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

   /**
    * Dispose of the disposable codebase model
    */
    public void dispose() throws RemoteException
    {
        VetoableDisposalEvent veto = new VetoableDisposalEvent( this );
        enqueueEvent( veto, false  );
        DisposalEvent disposal = new DisposalEvent( this );
        enqueueEvent( disposal, false );
        if( null != m_store )
        {
            m_store.remove();
        }
    }

    // ------------------------------------------------------------------------
    // CodeBaseModel
    // ------------------------------------------------------------------------

   /**
    * Set the codebase uri value.
    * @param uri the codebase uri
    */
    public void setCodeBaseURI( URI uri )
    {
        setCodeBaseURI( uri, true );
    }

   /**
    * Set the codebase uri value.
    * @param uri the codebase uri
    * @param notify if TRUE raise a notification event
    */
    protected void setCodeBaseURI( URI uri, boolean notify )
    {
        synchronized( m_lock )
        {
            m_uri = uri;
            if( null != m_store )
            {
                m_store.setCodeBaseURI( uri );
            }
            if( notify )
            {
                CodeBaseEvent e = new CodeBaseEvent( this, m_uri );
                super.enqueueEvent( e );
            }
        }
    }

   /**
    * Return the codebase uri.
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        synchronized( m_lock )
        {
            return m_uri;
        }
    }

   /**
    * Add a codebase listener to the model.
    * @param listener the listener to add
    */
    public void addCodeBaseListener( CodeBaseListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a codebase listener from the model.
    * @param listener the listener to remove
    */
    public void removeCodeBaseListener( CodeBaseListener listener )
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    protected void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof DisposalEvent )
        {
            processDisposalEvent( (DisposalEvent) eventObject );
        }
        else if( eventObject instanceof CodeBaseEvent )
        {
            CodeBaseEvent event = (CodeBaseEvent) eventObject;
            processCodeBaseEvent( event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + eventObject.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processCodeBaseEvent( CodeBaseEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof CodeBaseListener )
            {
                CodeBaseListener pl = (CodeBaseListener) listener;
                try
                {
                    pl.codeBaseChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "CodeBaseListener notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }


    private void processDisposalEvent( DisposalEvent eventObject )
    {
        DisposalEvent event = (DisposalEvent) eventObject;
        EventListener[] listeners = listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof DisposalListener )
            {
                DisposalListener pl = (DisposalListener) listener;
                if( event instanceof VetoableDisposalEvent )
                {
                    try
                    {
                        pl.disposing( event );
                    }
                    catch( RemoteException e )
                    {
                        final String error =
                          "Disposal listener remote notification error."; 
                        getLogger().error( error, e );
                    }
                }
                else
                {
                    try
                    {
                        pl.disposed( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "Disposal listener notification error."; 
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    private static class VetoableDisposalEvent extends DisposalEvent
    {
        public VetoableDisposalEvent( CodeBaseModel source )
        {
            super( source );
        }
    }

}


