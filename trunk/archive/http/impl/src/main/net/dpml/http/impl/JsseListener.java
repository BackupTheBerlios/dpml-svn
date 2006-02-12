/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http.impl;

import java.io.IOException;
import java.net.UnknownHostException;

import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpService;

import org.mortbay.http.HttpListener;

/** Wrapper for the Jetty SslListener.
 *
 */
public class JsseListener extends org.mortbay.http.SslListener
    implements HttpListener, Startable
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Return the assigned http server.
        * @return the server
        */
        HttpService getServer();
        
       /**
        * Return the buffer reserve value.
        * @param value the default reserve value
        * @return the buffer reserve
        */
        int getBufferReserve( int value );
        
       /**
        * Return the buffer size value.
        * @param value the default size value
        * @return the buffer size
        */
        int getBufferSize( int value );
        
       /**
        * Return the confidential port.
        * @param value the default value
        * @return the resolved port
        */
        int getConfidentialPort( int value );
        
       /**
        * Return the confidential scheme.
        * @param value the default value
        * @return the resolved value
        */
        String getConfidentialScheme( String value );
        
       /**
        * Return the default scheme.
        * @param value the default value
        * @return the resolved value
        */
        String getDefaultScheme( String value );
        
       /**
        * Return the integral port value.
        * @param value the default value
        * @return the resolved value
        */
        int getIntegralPort( int value );
        
       /**
        * Return the integral scheme.
        * @param value the default value
        * @return the resolved value
        */
        String getIntegralScheme( String value );
        
       /**
        * Return the host name
        * @param value the default value
        * @return the resolved value
        */
        String getHostName( String value );
        
       /**
        * Return the host port
        * @param value the default value
        * @return the resolved value
        */
        int getPort( int value );
        
       /**
        * Return the low resource persist ms value.
        * @param value the default value
        * @return the resolved value
        */
        int getLowResourcePersistMs( int value );
        
       /**
        * Return the identify-listener policy.
        * @param value the default policy value
        * @return the resolved policy
        */
        boolean getIdentifyListenerPolicy( boolean value );
        
       /**
        * Return the need-client-authentication policy.
        * @param value the default policy value
        * @return the resolved policy
        */
        boolean getNeedClientAuthentication( boolean value );
        
       /**
        * Return the key password.
        * @param value the default value
        * @return the resolved value
        */
        String getKeyPassword( String value );
        
       /**
        * Return the key store.
        * @param value the default value
        * @return the resolved value
        */
        String getKeyStore( String value );
        
       /**
        * Return the key store provider class.
        * @param value the default value
        * @return the resolved value
        */
        String getKeystoreProviderClass( String value );
        
       /**
        * Return the key store provider name.
        * @param value the default value
        * @return the resolved value
        */
        String getKeystoreProviderName( String value );
        
       /**
        * Return the key store type
        * @param value the default value
        * @return the resolved value
        */
        String setKeystoreType( String value );
        
       /**
        * Return the password.
        * @param value the default value
        * @return the resolved value
        */
        String getPassword( String value );
    }

    private HttpService m_httpServer;
    private Logger m_logger;

   /**
    * Creation of a new JsseListener instance.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception UnknownHostException if the hostname is unkown
    */
    public JsseListener( Logger logger, Context context ) throws UnknownHostException
    {
        m_logger = logger;
        m_httpServer = (HttpService) context.getServer();
        m_httpServer.addListener( this );

        int reserve = context.getBufferReserve( -1 );
        if( reserve > 0 )
        {
            setBufferReserve( reserve );
        }

        int size = context.getBufferSize( -1 );
        if( size > 0 )
        {
            setBufferSize( size );
        }

        int confPort = context.getConfidentialPort( -1 );
        if( confPort > 0 )
        {
            setConfidentialPort( confPort );
        }

        String confScheme = context.getConfidentialScheme( null );
        if( confScheme != null )
        {
            setConfidentialScheme( confScheme );
        }

        String defaultScheme = context.getDefaultScheme( null );
        if( defaultScheme != null )
        {
            setDefaultScheme( defaultScheme );
        }

        int integralPort = context.getIntegralPort( -1 );
        if( integralPort > 0 )
        {
            setIntegralPort( integralPort );
        }

        String integralScheme = context.getIntegralScheme( null );
        if( integralScheme != null )
        {
            setIntegralScheme( integralScheme );
        }

        String host = context.getHostName( null );
        if( host != null )
        {
            setHost( host );
        } 

        int port = context.getPort( 8080 );
        setPort( port );

        int lowResMs = context.getLowResourcePersistMs( -1 );
        if( lowResMs > 0 )
        {
            setLowResourcePersistTimeMs( lowResMs );
        }

        boolean identify = context.getIdentifyListenerPolicy( false );
        setIdentifyListener( identify );

        boolean needClientAuth = context.getNeedClientAuthentication( false );
        setNeedClientAuth( needClientAuth );

        String keyPass = context.getKeyPassword( null );
        if( keyPass != null )
        {
            setKeyPassword( keyPass );
        }

        String keyStore = context.getKeyStore( null );
        if( keyStore != null )
        {
            setKeystore( keyStore );
        }

        String keyStoreType = context.setKeystoreType( null );
        if( keyStoreType != null )
        {
            setKeystoreType( keyStoreType );
        }

        String password = context.getPassword( "" );
        setPassword( password );
    }

   /**
    * Start the listener.
    * @exception Exception if an error occurs
    */
    public void start() throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting SSL socket: " + this );
        }
        try
        {
            if( !isStarted() )
            {
                super.start();
            }
        } 
        catch( IOException e )
        {
            final String error = 
              "Unable to start SSL. "
              + "Possibly missing password to KeyStore or an invalid KeyStore.";
            m_logger.warn( error );
        }
    }

   /**
    * Stop the listener.
    * @exception InterruptedException if interrupted
    */
    public void stop() throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping SSL socket: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_httpServer.removeListener( this );
    }
}

