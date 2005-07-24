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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.EventObject;
import java.util.EventListener;
import java.net.PasswordAuthentication; 
import java.net.URL; 

import net.dpml.transit.store.ProxyStorage;

import net.dpml.transit.network.RequestIdentifier;

/**
 * The ProxyManager class maintains an active configuration model of the 
 * Transit proxy settings.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: StandardTransitDirector.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public class DefaultProxyModel extends DefaultModel implements ProxyModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ProxyStorage m_home;

    private URL m_host;
    private PasswordAuthentication m_authentication;
    private String[] m_excludes;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new proxy model.
    * @param logger the assigned logging channel
    * @param home the proxy model stroage home
    * @exception NullPointerException if the logging channel or home are null
    */
    public DefaultProxyModel( Logger logger, ProxyStorage home ) 
      throws NullPointerException, RemoteException
    {
        super( logger );

        if( null == home )
        {
            throw new NullPointerException( "home" );
        }

        m_home = home;
        m_host = home.getHost();
        m_authentication = home.getAuthentication();
        m_excludes = home.getExcludes();
    }

   /**
    * Construction of a new proxy model.
    * @param logger the assigned logging channel
    * @param host the proxy host url
    * @param auth the proxy authentication settings
    * @param excludes a set of proxy excludes
    * @exception NullPointerException if the logging channel is null
    */
    public DefaultProxyModel( 
      Logger logger, URL host, PasswordAuthentication auth, String[] excludes ) 
      throws NullPointerException, RemoteException
    {
        super( logger );

        m_home = null;
        m_host = host;
        m_authentication = auth;
        m_excludes = excludes;
    }

    // ------------------------------------------------------------------------
    // ProxyModel
    // ------------------------------------------------------------------------

   /**
    * Update the state of the proxy model.
    * @param host the proxy host
    * @param auth the proxy host authentication settings
    * @param excludes the set of proxy excludes
    */   
    public void update( URL host, PasswordAuthentication auth, String[] excludes )
    {
        synchronized( m_lock )
        {
            m_host = host;
            m_authentication = auth;
            m_excludes = excludes;

            if( null != m_home )
            {
                m_home.saveProxySettings( host, auth, excludes );
            }

            RequestIdentifier request = getRequestIdentifier( m_host );
            ProxyEvent event = 
              new ProxyEvent( this, request, m_authentication, m_excludes );
            super.enqueueEvent( event );
        }
    }

   /**
    * Update the proxy host url.
    * @param host the proxy host
    */
    public void setHost( URL host )
    {
        synchronized( m_lock )
        {
            m_host = host;

            if( null != m_home )
            {
                m_home.setHost( host );
            }

            RequestIdentifier request = getRequestIdentifier( m_host );
            ProxyEvent event = 
              new ProxyEvent( this, request, m_authentication, m_excludes );
            super.enqueueEvent( event );
        }
    }

   /**
    * Update the proxy excludes
    * @param excludes the proxy excludes
    */
    public void setExcludes( String[] excludes )
    {
        synchronized( m_lock )
        {
            m_excludes = excludes;

            if( null != m_home )
            {
                m_home.setExcludes( excludes );
            }

            RequestIdentifier request = getRequestIdentifier( m_host );
            ProxyEvent event = 
              new ProxyEvent( this, request, m_authentication, m_excludes );
            super.enqueueEvent( event );
        }
    }

   /**
    * Update the proxy authentication settings.
    * @param auth the proxy authentication settings
    */
    public void setAuthentication( PasswordAuthentication auth )
    {
        synchronized( m_lock )
        {
            m_authentication = auth;

            if( null != m_home )
            {
                m_home.setAuthentication( auth );
            }

            RequestIdentifier request = getRequestIdentifier( m_host );
            ProxyEvent event = 
              new ProxyEvent( this, request, m_authentication, m_excludes );
            super.enqueueEvent( event );
        }
    }

   /**
    * Return the proxy host name. 
    * @return the proxy host (possibly null)
    */
    public URL getHost()
    {
        return m_host;
    }

   /**
    * Return the proxy authentication or null if not defined.
    * @return the proxy authentication credentials
    */
    public PasswordAuthentication getAuthentication()
    {
        return m_authentication;
    }

   /**
    * Return the proxy host request identifier.
    * @return the request identifier for the proxy host or null if not defined.
    */
    public RequestIdentifier getRequestIdentifier()
    {
        return getRequestIdentifier( m_host );
    }

   /**
    * Return the set of excluded hosts as an array.
    * @return the excluded host array
    */
    public String[] getExcludes()
    {
        return m_excludes;
    }

   /**
    * Add a proxy listener to the model.
    * @param listener the listener to add
    */
    public void addProxyListener( ProxyListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a proxy listener from the model.
    * @param listener the listener to remove
    */
    public void removeProxyListener( ProxyListener listener )
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

    protected void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof ProxyEvent )
        {
            ProxyEvent event = (ProxyEvent) eventObject;
            processProxyEvent( event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + eventObject.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processProxyEvent( ProxyEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ProxyListener )
            {
                ProxyListener pl = (ProxyListener) listener;
                try
                {
                    pl.proxyChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Proxy listener notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

    private static final PasswordAuthentication NULL_AUTHENTICATION = 
      new PasswordAuthentication( "", new char[0] );

    private static RequestIdentifier getRequestIdentifier( URL url )
    {
        if( null == url )
        {
            return null;
        }
        else
        {
            String protocol = url.getProtocol();
            String scheme = "";
            String prompt = "";
            int port = url.getPort();
            String host = url.getHost();
            return new RequestIdentifier( host, port, protocol, scheme, prompt );
        }        
    }
}
