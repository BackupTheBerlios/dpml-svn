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

import java.net.UnknownHostException;

import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpService;
import net.dpml.http.spi.SocketListenerService;

import org.mortbay.http.HttpListener;

/**
 * Jetty SocketListener wrapper.
 */
public class SocketListener extends org.mortbay.http.SocketListener
    implements HttpListener, Startable, SocketListenerService 
{
   /**
    * Deployment context.
    */
    public interface Context
    {
       /**
        * Return the assigned http server.
        * @return the http server
        */
        HttpService getHttpServer();
        
       /**
        * Return the buffer reserve.
        * @param value the default value
        * @return the resolved value
        */
        int getBufferReserve( int value );
        
       /**
        * Return the buffer size.
        * @param value the default value
        * @return the resolved value
        */
        int getBufferSize( int value );
        
       /**
        * Return the confidential port.
        * @param value the default value
        * @return the resolved value
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
        * Return the integral port.
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
        * Return the host name.
        * @param value the default value
        * @return the resolved value
        */
        String getHostName( String value );
        
       /**
        * Return the port.
        * @param value the default value
        * @return the resolved value
        */
        int getPort( int value );
        
       /**
        * Return the low resource persist time in milliseconds.
        * @param value the default value
        * @return the resolved value
        */
        int getLowResourcePersistTimeMs( int value );
        
       /**
        * Return the identify listener policy.
        * @param value the default value
        * @return the resolved policy
        */
        boolean getIdentifyListener( boolean value );
    }

    private HttpService m_httpServer;
    private Logger m_logger;

   /**
    * Creation of a new SocketListener.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception UnknownHostException if the is unknown
    */
    public SocketListener( Logger logger, Context context ) throws UnknownHostException 
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

   /**
    * Start the handler.
    * @exception Exception if a startup error occurs
    */
    public void start()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting socket: " + this );
        }
        if( !isStarted() )
        {
            super.start();
        }
    }

   /**
    * Stop the handler.
    * @exception InterruptedException if interrupted
    */
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
