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

package net.dpml.transit;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication; 

import net.dpml.transit.info.HostDirective;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.HostListener;
import net.dpml.transit.model.HostChangeEvent;
import net.dpml.transit.model.HostLayoutEvent;
import net.dpml.transit.model.HostPriorityEvent;
import net.dpml.transit.model.HostNameEvent;
import net.dpml.transit.model.RequestIdentifier;
import net.dpml.transit.util.PropertyResolver;

import net.dpml.lang.UnknownKeyException;

/**
 * Default implementation of a host manager.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultHostModel extends DefaultModel implements HostModel, Comparable
{
    private static final int DEFAULT_PRIORITY = 600;

    private final LayoutRegistryModel m_registry;

    private String m_id;
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

    private Throwable m_error;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new host model.
    *
    * @param logger the assigned logging channel
    * @param directive the host configuration directive
    * @param registry the layout model registry
    * @exception UnknownKeyException if the layout id is unknown
    * @exception MalformedURLException if the host base url path is malformed
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultHostModel( Logger logger, HostDirective directive, LayoutRegistryModel registry ) 
      throws RemoteException, UnknownKeyException, MalformedURLException
    {
        super( logger );

        m_registry = registry;

        m_id = directive.getID();
        m_trusted = directive.getTrusted();
        m_enabled = directive.getEnabled();
        m_priority = directive.getPriority();

        m_base = resolveBaseValue( directive.getHost() );
        m_index = directive.getIndex();
        m_baseURL = resolveBaseURL( m_id, m_base );
        m_indexURL = resolveIndexURL( m_id, m_baseURL, m_index );
        
        String username = directive.getUsername();
        if( null != username )
        {
            m_authentication = new PasswordAuthentication( username, directive.getPassword() );
        }
        else
        {
            m_authentication = new PasswordAuthentication( null, new char[0] );
        }

        String key = directive.getLayout();
        m_layout = registry.getLayoutModel( key );
        String scheme = directive.getScheme();
        String prompt = directive.getPrompt();
        m_identifier = getRequestIdentifier( m_baseURL, scheme, prompt );
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

   /**
    * Dispose of the model.
    */
    void dispose()
    {
        super.dispose();
    }

    //----------------------------------------------------------------------
    // HostModel
    //----------------------------------------------------------------------

   /**
    * Return the host priority.
    * @return the host priority setting
    */
    public int getPriority()
    {
        synchronized( getLock() )
        {
            return m_priority;
        }
    }

   /**
    * Return the id of the resource host.  The value returned may be used to uniquely 
    * identify the host within the set of managed hosts. 
    */
    public String getID()
    {
        synchronized( getLock() )
        {
            return m_id;
        }
    }

   /**
    * Return the host base url path.
    * @return the base url path
    */
    public String getBasePath() 
    {
        synchronized( getLock() )
        {
            return m_base;
        }
    }

   /**
    * Return the host base url.
    * @return the base url
    */
    public URL getBaseURL()
    {
        synchronized( getLock() )
        {
            return m_baseURL;
        }
    }

   /**
    * Return index url path.
    * @return the index url path
    */
    public String getIndexPath()
    {
        synchronized( getLock() )
        {
            return m_index;
        }
    }

   /**
    * Return index url.
    * @return the index url
    */
    public URL getIndexURL()
    {
        synchronized( getLock() )
        {
            return m_indexURL;
        }
    }

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    */
    public boolean getEnabled()
    {
        synchronized( getLock() )
        {
            return m_enabled;
        }
    }

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    */
    public boolean getTrusted()
    {
        synchronized( getLock() )
        {
            return m_trusted;
        }
    }

   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    */
    public PasswordAuthentication getAuthentication()
    {
        synchronized( getLock() )
        {
            return m_authentication;
        }
    }

   /**
    * Return the host request identifier.
    * @return the identifier
    */
    public RequestIdentifier getRequestIdentifier()
    {
        synchronized( getLock() )
        {
            return m_identifier;
        }
    }

   /**
    * Return the layout strategy model.
    * @return the layout model
    */
    public LayoutModel getLayoutModel()
    {
        synchronized( getLock() )
        {
            return m_layout;
        }
    }

   /**
    * Add a host change listener to the model.
    * @param listener the host change listener to add
    */
    public void addHostListener( HostListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a host change listener from the model.
    * @param listener the host change listener to remove
    */
    public void removeHostListener( HostListener listener )
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
            String spec = PropertyResolver.resolve( path );
            return new URL( spec );
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

