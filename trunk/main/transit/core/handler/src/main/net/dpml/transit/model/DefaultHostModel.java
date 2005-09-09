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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication; 

import net.dpml.transit.Logger;
import net.dpml.transit.store.HostStorage;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.Strategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.LocalStrategy;
import net.dpml.transit.util.PropertyResolver;

/**
 * Default implementation of a host manager. The implementation establishes
 * a preferences change listener on a supplied preferences instance representing
 * a named host.  The immutable host id corresponds to the supplied preference
 * node name.  Features of the host are resolved from preference node attributes
 * and consildated under the HostModel implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class DefaultHostModel extends DisposableCodeBaseModel 
  implements HostModel, DisposalListener, Comparable
{
    private static final int DEFAULT_PRIORITY = 600;

    private final HostStorage m_home;
    private final LayoutRegistryModel m_registry;

    private String m_name;
    private String m_base;
    private String m_index;
    private URL m_baseURL;
    private URL m_indexURL;
    private boolean m_enabled = false;
    private boolean m_trusted = false;
    private LayoutModel m_layout;
    private int m_priority = DEFAULT_PRIORITY;
    private RequestIdentifier m_identifier;
    private PasswordAuthentication m_authentication;
    private boolean m_bootstrap = false;

    private Throwable m_error;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new host model.  
    *
    * @param logger the assigned logging channel
    * @param registry the layout model registry 
    * @param uri the codebase uri
    * @param params the codebase parameters
    * @param id the host model identifier
    * @param base the host base url path
    * @param index the host index path
    * @param name the human readable host name
    * @param trusted TRUE if this is a trusted host
    * @param enabled TRUE if this host model is enabled
    * @param priority the priority of this host
    * @param layout the id of the layout model to assign to the host model
    * @param auth a possibly null host authentication username and password
    * @param scheme the host security scheme
    * @param prompt the security prompt raised by the host
    * @param bootstrap TRUE if this is a bootstrap host
    * @exception UnknownKeyException if the layout id is unknown
    * @exception MalformedURLException if the host base url path is malformed
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultHostModel( 
      Logger logger, LayoutRegistryModel registry, URI uri, Value[] params, String id, String base, String index, 
      String name, boolean trusted, boolean enabled, int priority, String layout, 
      PasswordAuthentication auth, String scheme, String prompt, boolean bootstrap ) 
      throws RemoteException, UnknownKeyException, MalformedURLException
    {
        super( logger, id, uri, params );

        m_home = null;
        m_registry = registry;

        m_name = name;
        m_trusted = trusted;
        m_enabled = enabled;
        m_priority = priority;
        m_layout = registry.getLayoutModel( layout );
        m_base = resolveBaseValue( base );
        m_index = index;
        m_baseURL = resolveBaseURL( id, m_base );
        m_indexURL = resolveIndexURL( id, m_baseURL, m_index );
        m_authentication = auth;
        m_layout.addDisposalListener( this );
        m_identifier = getRequestIdentifier( m_baseURL, scheme, prompt );
        m_bootstrap = bootstrap;
    }

   /**
    * Creation of a new host model.
    *
    * @param logger the assigned logging channel
    * @param home the host persistent storage home
    * @param registry the layout model registry
    * @exception UnknownKeyException if the layout id is unknown
    * @exception MalformedURLException if the host base url path is malformed
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultHostModel( Logger logger, HostStorage home, LayoutRegistryModel registry ) 
      throws RemoteException, UnknownKeyException, MalformedURLException
    {
        super( logger, home );

        m_home = home;
        m_registry = registry;

        m_name = home.getName();
        m_trusted = home.getTrusted();
        m_enabled = home.getEnabled();
        m_priority = home.getPriority();

        m_base = resolveBaseValue( home.getBasePath() );
        m_index = home.getIndexPath();
        m_baseURL = resolveBaseURL( getID(), m_base );
        m_indexURL = resolveIndexURL( getID(), m_baseURL, m_index );
        m_authentication = home.getAuthentication();

        String key = home.getLayoutModelKey();
        m_layout = registry.getLayoutModel( key );
        m_layout.addDisposalListener( this );

        String scheme = home.getScheme();
        String prompt = home.getPrompt();
        m_identifier = getRequestIdentifier( m_baseURL, scheme, prompt );

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

   /**
    * Dispose of the model.
    * @exception RemoteException if a remote exception occurs
    */
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
    * The implementation will always throw a VetoDisposalException to declare usage 
    * of the layout model.
    * @param event the disposal warning event
    * @exception VetoDisposalException always thrown to veto layout removal
    * @exception RemoteException if a remote exception occurs
    */
    public void disposing( DisposalEvent event ) throws VetoDisposalException, RemoteException
    {
        final String id = getID();
        final String message = "Layout currently assigned to host: " + id;
        throw new VetoDisposalException( this, message );
    }

   /**
    * Notify the listener of the disposal of the layout. 
    * This method should never be invoked and will result in the logging 
    * of an error.
    * @exception RemoteException if a remote exception occurs
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

   /**
    * Update the state of the host model.
    *
    * @param base the host base url path
    * @param index the host content index
    * @param enabled the enabled status of the host
    * @param trusted the trusted status of the host
    * @param layout the assigned host layout identifier
    * @param auth a possibly null host authentication username and password
    * @param scheme the host security scheme
    * @param prompt the security prompt raised by the host
    * @exception UnknownKeyException if the layout id is unknown
    * @exception MalformedURLException if the host base url path is malformed
    * @exception BootstrapException if the host is a bootstrap host and a 
    *   non-bootstrap layout is assigned
    * @exception RemoteException if a remote exception occurs
    */
    public void update( 
      String base, String index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt ) 
      throws BootstrapException, UnknownKeyException, RemoteException, MalformedURLException
    {
        synchronized( getLock() )
        {
            LayoutModel layoutModel = m_registry.getLayoutModel( layout );
            setLayoutModel( layoutModel );

            m_base = resolveBaseValue( base );
            m_index = index;
            m_baseURL = resolveBaseURL( getID(), m_base );
            m_indexURL = resolveIndexURL( getID(), m_baseURL, m_index );

            m_enabled = enabled;
            m_trusted = trusted;
            m_authentication = auth;
            m_identifier = getRequestIdentifier( m_baseURL, scheme, prompt );
            
            if( null != m_home )
            {
                m_home.setHostSettings( base, index, enabled, trusted, layout, auth, scheme, prompt );
            }

            HostChangeEvent e = 
              new HostChangeEvent( 
                this, m_baseURL, m_indexURL, m_identifier, auth, enabled, trusted );
            enqueueEvent( e );
        }
    }

   /**
    * Set the human readable name of the host to the supplied value.
    * @param name the human readable name
    * @exception RemoteException if a remote exception occurs
    */
    public void setName( String name ) throws RemoteException
    {
        synchronized( getLock() )
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

   /**
    * Set the host priority to the supplied value.
    * @param priority the host priority
    * @exception RemoteException if a remote exception occurs
    */
    public void setPriority( int priority ) throws RemoteException
    {
        synchronized( getLock() )
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
    *
    * @param layout the layout model to assign
    * @exception BootstrapException if the host model is a bootstrap host and 
    *   the assigned layout model is not a bootstrap layout model
    * @exception RemoteException if a remote exception occurs
    */
    public void setLayoutModel( LayoutModel layout ) throws BootstrapException, RemoteException
    {
        synchronized( getLock() )
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

   /**
    * Return TRUE if this is a bootstrap host. Bootstrap hosts shall be 
    * provided such that they independent of the Transit respository 
    * service.
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isBootstrap() throws RemoteException
    {
        return m_bootstrap;
    }

   /**
    * Return the host priority.
    * @return the host priority setting
    * @exception RemoteException if a remote exception occurs
    */
    public int getPriority() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_priority;
        }
    }

   /**
    * Return the name of the resource host.  The value returned may be used to uniquely 
    * identify the host within the set of managed hosts. 
    * @exception RemoteException if a remote exception occurs
    */
    public String getHostName() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_name;
        }
    }

   /**
    * Return the host base url path.
    * @return the base url path
    * @exception RemoteException if a remote exception occurs
    */
    public String getBasePath() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_base;
        }
    }

   /**
    * Return the host base url.
    * @return the base url
    * @exception RemoteException if a remote exception occurs
    */
    public URL getBaseURL() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_baseURL;
        }
    }

   /**
    * Return index url path.
    * @return the index url path
    * @exception RemoteException if a remote exception occurs
    */
    public String getIndexPath() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_index;
        }
    }

   /**
    * Return index url.
    * @return the index url
    * @exception RemoteException if a remote exception occurs
    */
    public URL getIndexURL() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_indexURL;
        }
    }

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    * @exception RemoteException if a remote exception occurs
    */
    public boolean getEnabled() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_enabled;
        }
    }

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    * @exception RemoteException if a remote exception occurs
    */
    public boolean getTrusted() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_trusted;
        }
    }

   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    * @exception RemoteException if a remote exception occurs
    */
    public PasswordAuthentication getAuthentication() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_authentication;
        }
    }

   /**
    * Return the host request identifier.
    * @return the identifier
    * @exception RemoteException if a remote exception occurs
    */
    public RequestIdentifier getRequestIdentifier() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_identifier;
        }
    }

   /**
    * Return the layout strategy model.
    * @return the layout model
    * @exception RemoteException if a remote exception occurs
    */
    public LayoutModel getLayoutModel() throws RemoteException
    {
        synchronized( getLock() )
        {
            return m_layout;
        }
    }

   /**
    * Add a host change listener to the model.
    * @param listener the host change listener to add
    * @exception RemoteException if a remote exception occurs
    */
    public void addHostListener( HostListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a host change listener from the model.
    * @param listener the host change listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeHostListener( HostListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    //----------------------------------------------------------------------
    // Comparable
    //----------------------------------------------------------------------

   /**
    * Compare this host with another object.
    * @param other the object to compare with this host model
    * @return the comparitive value based on a comparison of host priorities
    */
    public int compareTo( Object other )
    {
        if( null == other )
        {
            return -1;
        }
        else if( !( other instanceof HostModel ) )
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

    //----------------------------------------------------------------------
    // internal
    //----------------------------------------------------------------------

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

    private void setEnabled( boolean enabled )
    {
        synchronized( getLock() )
        {
            m_enabled = enabled;
        }
    }

   /**
    * Internal event handler.
    * @param event the event to handle
    */
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
        for( int i=0; i < listeners.length; i++ )
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
        for( int i=0; i < listeners.length; i++ )
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
        for( int i=0; i < listeners.length; i++ )
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
        for( int i=0; i < listeners.length; i++ )
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
        for( int i=0; i < listeners.length; i++ )
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

    private static String resolveBaseValue( String path )
    {
        //
        // make sure the base path ends with a "/" otherwise relative url references 
        // will not be correct
        //

        if( !path.endsWith( "/" ) )
        {
            return path + "/";
        }
        else
        {
            return path;
        }
    }

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

    private static URL resolveBaseURL( String id, String path ) throws MalformedURLException
    {
        if( null == path )
        {
            return getDefaultHostURL();
        }
        try
        {
            return new URL( path );
        }
        catch( MalformedURLException e )
        {
            final String error =  
              "Invalid host base url"
              + "\nHost ID: " + id
              + "\nHost Path: " + path
              + "\nCause: " + e.getMessage();
            throw new MalformedURLException( error );
        }
    }

    private static URL resolveIndexURL( String id, URL base, String path ) throws MalformedURLException
    {
        if( null == path )
        {
            return null;
        }

        String resolved = PropertyResolver.resolve( path );

        try
        {
            return new URL( resolved );
        }
        catch( MalformedURLException e )
        {
            try
            {
                return new URL( base, resolved );
            }
            catch( MalformedURLException ee )
            {
                final String error =  
                  "Invalid index url"
                  + "\nHost ID: " + id
                  + "\nHost Path: " + base
                  + "\nIndex Path: " + path
                  + "\nCause: " + e.getMessage();
                throw new MalformedURLException( error );
            }
        }
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

