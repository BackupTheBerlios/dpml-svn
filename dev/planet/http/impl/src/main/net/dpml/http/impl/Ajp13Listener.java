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

import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.parameters.ParameterException;
import net.dpml.parameters.Parameterizable;
import net.dpml.parameters.Parameters;
import net.dpml.http.HttpService;
import org.mortbay.http.HttpListener;

/** Wrapper for the Jetty SocketListener.
 *
 * @metro.component name="http-socket-listener" lifestyle="singleton"
 * @metro.service type="org.mortbay.http.HttpListener"
 */
public class Ajp13Listener extends org.mortbay.http.ajp.AJP13Listener
    implements HttpListener, Startable
{
    public interface Context
    {
        HttpService getHttpServer();
        int getBufferReserve( int value );
        int getBufferSize( int value );
        int getConfidentialPort( int value );
        String getConfidentialScheme( String value );
        int getIntegralPort( int value );
        String getIntegralScheme( String value );
        String getHostName( String value );
        int getPort( int value );
        boolean getIdentifyListenerPolicy( boolean value );
    }

    private HttpService m_httpServer;
    private Logger m_logger;

    /**
     */
    public Ajp13Listener( Logger logger, Context context )
        throws ParameterException
    {
        m_logger = logger;
        m_httpServer = context.getHttpServer();
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
        try
        {
            if( host != null )
            {
                setHost( host );
            }
        } 
        catch( java.net.UnknownHostException e )
        {
            throw new ParameterException( "Unknown hostname: " + host );
        }

        int port = context.getPort( 8080 );
        setPort( port );

        boolean identify = context.getIdentifyListenerPolicy( false );
        setIdentifyListener( identify );
    }

    public void start() throws Exception
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
        m_httpServer.removeListener( this );
    }
}

