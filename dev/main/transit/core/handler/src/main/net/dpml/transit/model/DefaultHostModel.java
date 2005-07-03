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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.PasswordAuthentication; 

import net.dpml.transit.store.HostStorage;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.Strategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.LocalStrategy;

import net.dpml.transit.network.RequestIdentifier;

/**
 * Default implementation of a host manager. The implementation establishes
 * a preferences change listener on a supplied preferences instance representing
 * a named host.  The immutable host id corresponds to the supplied preference
 * node name.  Features of the host are resolved from preference node attributes
 * and consildated under the HostModel implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultHostModel extends DisposableCodeBaseModel 
  implements HostModel, DisposalListener, Comparable
{
    private final HostStorage m_home;
    private final LayoutRegistryModel m_registry;
    private final String m_id;

    private String m_name;
    private URL m_base;
    private URL m_index;
    private boolean m_enabled = false;
    private boolean m_trusted = false;
    private LayoutModel m_layout;
    private int m_priority = 600;
    private RequestIdentifier m_identifier;
    private PasswordAuthentication m_authentication;
    private boolean m_bootstrap = false;

    private Throwable m_error;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultHostModel( 
      Logger logger, LayoutRegistryModel registry, URI uri, String id, URL base, URL index, 
      String name, boolean trusted, boolean enabled, int priority, String layout, 
      PasswordAuthentication auth, String scheme, String prompt, boolean bootstrap ) 
      throws RemoteException, UnknownKeyException 
    {
        super( logger, uri );

        m_home = null;
        m_registry = registry;

        m_id = id;
        m_name = name;
        m_trusted = trusted;
        m_enabled = enabled;
        m_priority = priority;
        m_layout = registry.getLayoutModel( layout );
        m_base = base;
        m_index = index;
        m_authentication = auth;

        m_layout.addDisposalListener( this );
        m_identifier = getRequestIdentifier( base, scheme, prompt );
        m_bootstrap = bootstrap;
    }

    public DefaultHostModel( Logger logger, HostStorage home, LayoutRegistryModel registry ) 
      throws RemoteException, UnknownKeyException 
    {
        super( logger, home );

        m_home = home;
        m_registry = registry;

        m_id = home.getID();
        m_name = home.getName();
        m_trusted = home.getTrusted();
        m_enabled = home.getEnabled();
        m_priority = home.getPriority();
        m_base = home.getBaseURL();
        m_index = home.getIndexURL( m_base );
        m_authentication = home.getAuthentication();

        String key = home.getLayoutModelKey();
        m_layout = registry.getLayoutModel( key );
        m_layout.addDisposalListener( this );

        String scheme = home.getScheme();
        String prompt = home.getPrompt();
        m_identifier = getRequestIdentifier( m_base, scheme, prompt );

        Strategy strategy = home.getStrategy();
        if( strategy instanceof PluginStrategy )
        {
            PluginStrategy plugin = (PluginStrategy) strategy;
            setCodeBaseURI( plugin.getURI(), false );
            m_bootstrap = false;
        }
        else
        {
            LocalStrategy local = (LocalStrategy) strategy;
            m_bootstrap = local.isBootstrap();
        }
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

    public void dispose() throws RemoteException
    {
        super.dispose();
        m_layout.removeDisposalListener( this );
        if( ( null != m_home ) && ( m_home instanceof Removable ) )
        {
            Removable store = (Removable) m_home;
            store.remove();
        }
    }

    // ------------------------------------------------------------------------
    // DisposalListener (listen to modification to the assigned layout)
    // ------------------------------------------------------------------------

   /**
    * Notify the listener of the disposal of a layout.
    */
    public void disposing( DisposalEvent event ) throws VetoDisposalException, RemoteException
    {
        final String id = getID();
        final String message = "Layout currently assigned to host: " + id;
        throw new VetoDisposalException( this, message );
    }

   /**
    * Notify the listener of the disposal of the layout.
    */
    public void disposed( DisposalEvent event ) throws RemoteException // should never happen
    {
        setEnabled( false );
        final String error = 
          "Internal error."
          + "\nUnexpected notification of disposal of an assigned layout."
          + "\nHost set to disabled state."
          + "\nHost: " + getID()
          + "\nLayout: " + m_layout.getID();
        getLogger().error( error );
    }

    //----------------------------------------------------------------------
    // HostModel
    //----------------------------------------------------------------------

    public void update( 
      URL base, URL index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt ) 
      throws BootstrapException, UnknownKeyException, RemoteException 
    {
        synchronized( m_lock )
        {
            LayoutModel layoutModel = m_registry.getLayoutModel( layout );
            setLayoutModel( layoutModel );

            m_base = base;
            m_index = index;
            m_enabled = enabled;
            m_trusted = trusted;
            m_authentication = auth;
            m_identifier = getRequestIdentifier( base, scheme, prompt );
            
            if( null != m_home )
            {
                m_home.setHostSettings( base, index, enabled, trusted, layout, auth, scheme, prompt );
            }

            HostChangeEvent e = 
              new HostChangeEvent( 
                this, base, index, m_identifier, auth, enabled, trusted );
            enqueueEvent( e );
        }
    }

    public void setName( String name ) throws RemoteException
    {
        synchronized( m_lock )
        {
            m_name = name;

            if( null != m_home )
            {
                m_home.setName( name );
            }

            HostNameEvent e = new HostNameEvent( this, m_name );
            enqueueEvent( e );
        }
    }

    public void setPriority( int priority ) throws RemoteException
    {
        synchronized( m_lock )
        {
            m_priority = priority;

            if( null != m_home )
            {
                m_home.setPriority( priority );
            }

            HostPriorityEvent e = new HostPriorityEvent( this, m_priority );
            enqueueEvent( e );
        }
    }

   /**
    * If this is a bootstrap host then resolver ids must map to
    * a resolver model that is based on a classname as opposed to 
    * plugin (because bootstrap hosts are repository indepedent)
    */
    public void setLayoutModel( LayoutModel layout ) throws BootstrapException, RemoteException
    {
        synchronized( m_lock )
        {
            checkLayout( layout );
            m_layout.removeDisposalListener( this );
            layout.addDisposalListener( this );
            m_layout = layout;

            if( null != m_home )
            {
                String id = layout.getID();
                m_home.setLayoutModelKey( id );
            }

            HostLayoutEvent e = new HostLayoutEvent( this, m_layout );
            enqueueEvent( e );
        }
    }

    private void checkLayout( LayoutModel layout ) throws BootstrapException, RemoteException
    {
        if( isBootstrap() )
        {
            if( null != layout.getCodeBaseURI() )
            {
                final String error = 
                  "Illegal attempt to assign a plugin based layout to a bootstrap host."
                  + "\nHost ID: " + getID()
                  + "\nLayout ID: " + layout.getID();
                throw new BootstrapException( error );
            }
        }
    }

    //----------------------------------------------------------------------
    // Comparable
    //----------------------------------------------------------------------

    public int compareTo( Object other )
    {
        if( null == other )
        {
            return -1;
        }
        else if( false == other instanceof HostModel )
        {
            return -1;
        }
        else
        {
            try
            {
                HostModel host = (HostModel) other;
                Integer i = new Integer( getPriority() );
                Integer j = new Integer( host.getPriority() );
                return i.compareTo( j );
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unable to compare host due to a remote exception.";
                throw new ModelRuntimeException( error, e );
            }
        }
    }

   /**
    * Return an immutable host identifier.  The host identifier shall be 
    * guranteed to be unique and constant for the life of the model.
    */
    public String getID() throws RemoteException
    {
        return m_id;
    }

   /**
    * Return TRUE if this is a bootstrap host. Bootstrap hosts shall be 
    * provided such that they independent of the Transit respository 
    * service.
    */
    public boolean isBootstrap() throws RemoteException
    {
        return m_bootstrap;
    }

   /**
    * Return the host priority.
    * @return the host priority setting
    */
    public int getPriority() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_priority;
        }
    }

   /**
    * Return the name of the resource host.  The value returned may be used to uniquely 
    * identify the host within the set of managed hosts. 
    */
    public String getHostName() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_name;
        }
    }

   /**
    * Return the host base url.
    * @return the base url
    */
    public URL getBaseURL() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_base;
        }
    }

   /**
    * Return index url.
    * @return the index url
    */
    public URL getIndexURL() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_index;
        }
    }

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    */
    public boolean getEnabled() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_enabled;
        }
    }

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    */
    public boolean getTrusted() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_trusted;
        }
    }

   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    */
    public PasswordAuthentication getAuthentication() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_authentication;
        }
    }

   /**
    * Return the host request identifier.
    * @return the identifier
    */
    public RequestIdentifier getRequestIdentifier() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_identifier;
        }
    }

   /**
    * Return the layout strategy model.
    * @return the layout model
    */
    public LayoutModel getLayoutModel() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_layout;
        }
    }

   /**
    * Add a host change listener to the model.
    * @param listener the host change listener to add
    */
    public void addHostListener( HostListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a host change listener from the model.
    * @param listener the host change listener to remove
    */
    public void removeHostListener( HostListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // internal
    //----------------------------------------------------------------------

    private void setEnabled( boolean enabled )
    {
        synchronized( m_lock )
        {
            m_enabled = enabled ;
        }
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof HostChangeEvent )
        {
            processHostChangeEvent( (HostChangeEvent) event );
        }
        else if( event instanceof HostLayoutEvent )
        {
            processHostLayoutEvent( (HostLayoutEvent) event );
        }
        else if( event instanceof HostPriorityEvent )
        {
            processHostPriorityEvent( (HostPriorityEvent) event );
        }
        else if( event instanceof HostNameEvent )
        {
            processHostNameEvent( (HostNameEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processHostLayoutEvent( HostLayoutEvent event )
    {
        HostListener[] listeners = getHostListeners();
        for( int i=0; i<listeners.length; i++ )
        {
            HostListener listener = listeners[i];
            try
            {
                listener.layoutChanged( event );
            }
            catch( Throwable e )
            {
                final String error =
                  "HostListener resolver change notification error.";
                getLogger().warn( error, e );
            }
        }
    }

    private void processHostPriorityEvent( HostPriorityEvent event )
    {
        HostListener[] listeners = getHostListeners();
        for( int i=0; i<listeners.length; i++ )
        {
            HostListener listener = listeners[i];
            try
            {
                listener.priorityChanged( event );
            }
            catch( Throwable e )
            {
                final String error =
                  "HostListener priority change notification error.";
                getLogger().warn( error, e );
            }
        }
    }

    private void processHostNameEvent( HostNameEvent event )
    {
        HostListener[] listeners = getHostListeners();
        for( int i=0; i<listeners.length; i++ )
        {
            HostListener listener = listeners[i];
            try
            {
                listener.nameChanged( event );
            }
            catch( Throwable e )
            {
                final String error =
                  "HostListener name change notification error.";
                getLogger().warn( error, e );
            }
        }
    }

    private void processHostChangeEvent( HostChangeEvent event )
    {
        HostListener[] listeners = getHostListeners();
        for( int i=0; i<listeners.length; i++ )
        {
            HostListener listener = listeners[i];
            try
            {
                listener.hostChanged( event );
            }
            catch( Throwable e )
            {
                final String error =
                  "HostListener change notification error.";
                getLogger().warn( error, e );
            }
        }
    }

    private HostListener[] getHostListeners()
    {
        ArrayList list = new ArrayList();
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof HostListener )
            {
                HostListener listener = (HostListener) eventListener;
                list.add( listener );
            }
        }
        return (HostListener[]) list.toArray( new HostListener[0] ); 
    }

    // ------------------------------------------------------------------------
    // static (utils)
    // ------------------------------------------------------------------------

    private static RequestIdentifier getRequestIdentifier( URL base, String scheme, String prompt )
    {
        if( null == base )
        {
            throw new NullPointerException( "base" );
        }
        if( null == scheme )
        {
            throw new NullPointerException( "scheme" );
        }
        if( null == prompt )
        {
            throw new NullPointerException( "prompt" );
        }
        String protocol = base.getProtocol();
        String host = base.getHost();
        int port = base.getPort();
        if( port == 0 )
        {
            if( protocol.equals( "http" ) )
            {
                port = HTTP_PORT;
            }
            else if( protocol.equals( "ftp" ) )
            {
                port = FTP_PORT;
            }
            else if( protocol.equals( "https" ) )
            {
                port = HTTPS_PORT;
            }
        }
        return new RequestIdentifier( host, port, protocol, scheme, prompt );
    }

    private static URL getDefaultHostURL()
    {
        try
        {
            return new URL( "http://localhost" );
        }
        catch( Exception e )
        {
            return null;
        }
    }

}

