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
import net.dpml.parameters.ParameterException;
import net.dpml.parameters.Parameterizable;
import net.dpml.parameters.Parameters;
import net.dpml.http.spi.HttpService;

import org.mortbay.http.HttpListener;

/** 
 * Wrapper for the Jetty SocketListener.
 */
public class Ajp13Listener extends org.mortbay.http.ajp.AJP13Listener
    implements HttpListener, Startable
{
   /**
    * Component context model.
    */
    public interface Context
    {
       /**
        * Get the HTTP server.
        * @return the HTTP server
        */
        HttpService getHttpServer();
        
       /**
        * Get the buffer reserve.
        * @param value the default value
        * @return the buffer reserve
        */
        int getBufferReserve( int value );
        
       /**
        * Get the buffer size.
        * @param value the default value
        * @return the buffer size
        */
        int getBufferSize( int value );
        
       /**
        * Get the confidential port.
        * @param value the default value
        * @return the port number
        */
        int getConfidentialPort( int value );
        
       /**
        * Get the confidential port scheme.
        * @param value the default value
        * @return the confidential port scheme
        */
        String getConfidentialScheme( String value );
        
       /**
        * Get the integral port.
        * @param value the default value
        * @return the integral port
        */
        int getIntegralPort( int value );
        
       /**
        * Get the integral port scheme.
        * @param value the default value
        * @return the integral port scheme
        */
        String getIntegralScheme( String value );
        
       /**
        * Get the host name.
        * @param value the default host name
        * @return the host name
        */
        String getHostName( String value );
        
       /**
        * Get the port.
        * @param value the default port
        * @return the configured port
        */
        int getPort( int value );
        
       /**
        * Get the pidentify listener policy.
        * @param value the default value
        * @return the policy value
        */
        boolean getIdentifyListenerPolicy( boolean value );
    }

    private HttpService m_httpServer;
    private Logger m_logger;

   /**
    * Creation of a new Ajp13Listener.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception UnknownHostException if the host name is unknown
    */
    public Ajp13Listener( Logger logger, Context context )
        throws UnknownHostException
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
        if( host != null )
        {
            setHost( host );
        } 

        int port = context.getPort( 8080 );
        setPort( port );

        boolean identify = context.getIdentifyListenerPolicy( false );
        setIdentifyListener( identify );
    }

   /**
    * Start the component.
    * @exception Exception if an error occurs during startup
    */
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

   /**
    * Stop the component.
    * @exception InterruptedException if the stop process is interrupted
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

