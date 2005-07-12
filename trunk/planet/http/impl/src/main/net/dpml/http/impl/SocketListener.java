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

import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpService;
import org.mortbay.http.HttpListener;

public class SocketListener extends org.mortbay.http.SocketListener
    implements HttpListener, Startable
{
    public interface Context
    {
        HttpService getHttpServer();
        int getBufferReserve( int value );
        int getBufferSize( int value );
        int getConfidentialPort( int value );
        String getConfidentialScheme( String value );
        String getDefaultScheme( String value );
        int getIntegralPort( int value );
        String getIntegralScheme( String value );
        String getHostName( String value );
        int getPort( int value );
        int getLowResourcePersistTimeMs( int value );
        boolean getIdentifyListener( boolean value );
    }

    private HttpService m_HttpServer;
    private Logger      m_logger;

    public SocketListener(Logger logger, Context context ) throws java.net.UnknownHostException 
    {
        m_logger = logger;
        m_HttpServer = context.getHttpServer();
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

        String defScheme = context.getDefaultScheme( null );
        if( defScheme != null )
        {
            setDefaultScheme( defScheme );
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

        int lowResMs = context.getLowResourcePersistTimeMs( -1 );
        if( lowResMs > 0 )
        {
            setLowResourcePersistTimeMs( lowResMs );
        }

        boolean identify = context.getIdentifyListener( false );
        setIdentifyListener( identify );
    }

    public void start()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting socket: " + this );
        }
        if( ! isStarted() )
        {
            super.start();
        }
    }

    public void stop() throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping socket: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_HttpServer.removeListener( this );
    }
}
