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
import java.util.EventObject;
import java.util.EventListener;
import java.net.MalformedURLException; 
import java.net.PasswordAuthentication; 
import java.net.URL; 

import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.ProxyListener;
import net.dpml.transit.model.ProxyEvent;
import net.dpml.transit.model.RequestIdentifier;

/**
 * The ProxyModel class maintains an active configuration model of the 
 * Transit proxy configuration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultProxyModel extends DefaultModel implements ProxyModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private URL m_host;
    private PasswordAuthentication m_authentication;
    private String[] m_excludes;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new proxy model.
    * @param logger the assigned logging channel
    * @param directive the proxy congfiguration
    * @exception NullPointerException if the logging channel or directive arguments are null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultProxyModel( Logger logger, ProxyDirective directive ) 
      throws NullPointerException, MalformedURLException, RemoteException
    {
        super( logger );

        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }

        m_host = new URL( directive.getHost() );
        String username = directive.getUsername();
        if( null != username )
        {
            m_authentication = 
              new PasswordAuthentication( username, directive.getPassword() );
        }
        else
        {
            m_authentication = NULL_AUTHENTICATION;
        }
        m_excludes = directive.getExcludes();
    }

    // ------------------------------------------------------------------------
    // ProxyModel
    // ------------------------------------------------------------------------

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

    public void dispose()
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            removeListener( listener );
        }
    }

   /**
    * Internal event handler.
    * @param eventObject the event to handle
    */
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
        for( int i=0; i < listeners.length; i++ )
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
