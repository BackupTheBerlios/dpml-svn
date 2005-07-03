/*
 * Copyright 2004 Niclas Hedman.
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

import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.http.HttpService;
import org.mortbay.http.HttpListener;

/** Wrapper for the Jetty SocketListener.
 *
 * @metro.component name="http-socket-listener" lifestyle="singleton"
 * @metro.service type="org.mortbay.http.HttpListener"
 */
public class JsseListener extends org.mortbay.http.SunJsseListener
    implements HttpListener, Startable
{
    public interface Context
    {
        HttpService getServer();
        int getBufferReserve( int value );
        int getBufferSize( int value );
        int getConfidentialPort( int value );
        String getConfidentialScheme( String value );
        String getDefaultScheme( String value );
        int getIntegralPort( int value );
        String getIntegralScheme( String value );
        String getHostName( String value );
        int getPort( int value );
        int getLowResourcePersistMs( int value );
        boolean getIdentifyListenerPolicy( boolean value );
        boolean getNeedClientAuthentication( boolean value );
        boolean getUseDefaultTrustStore( boolean value );
        String getKeyPassword( String value );
        String getKeyStore( String value );
        String getKeystoreProviderClass( String value );
        String getKeystoreProviderName( String value );
        String setKeystoreType( String value );
        String getPassword( String value );
    }

    private HttpService m_HttpServer;
    private Logger      m_logger;

    /**
     *
     * @metro.logger name="http"
     * @metro.dependency type="net.dpml.http.HttpService"
     *                   key="server"
     */
    public JsseListener( Logger logger, Context context ) throws java.net.UnknownHostException
    {
        m_logger = logger;
        m_HttpServer = (HttpService) context.getServer();
        m_HttpServer.addListener( this );

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

        boolean useDefTrustStore = context.getUseDefaultTrustStore( false );
        setUseDefaultTrustStore( useDefTrustStore );

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

        String keyStoreProviderClass = context.getKeystoreProviderClass( null );
        if( keyStoreProviderClass != null )
        {
            setKeystoreProviderClass( keyStoreProviderClass );
        }

        String keyStoreProviderName = context.getKeystoreProviderName( null );
        if( keyStoreProviderName != null )
        {
            setKeystoreProviderName( keyStoreProviderName );
        }

        String keyStoreType = context.setKeystoreType( null );
        if( keyStoreType != null )
        {
            setKeystoreType( keyStoreType );
        }

        String password = context.getPassword( "" );
        setPassword( password );
    }

    public void start() throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting SSL socket: " + this );
        }
        try
        {
            if( ! isStarted() )
            {
                super.start();
            }
        } 
        catch( IOException e )
        {
            final String error = 
              "Unable to start SSL. "
              + "Possibly missing password to KeyStore or an invalid KeyStore." ;
            m_logger.warn( error );
        }
    }

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
        m_HttpServer.removeListener( this );
    }
}

