/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.http.impl;

import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.http.HttpService;

import org.mortbay.http.HttpListener;

/** Wrapper for the Jetty SocketListener.
 *
 * @avalon.component name="http-socket-listener" lifestyle="singleton"
 * @avalon.service type="org.mortbay.http.HttpListener"
 */
public class JsseListener extends org.mortbay.http.SunJsseListener
    implements Parameterizable, Startable, Serviceable, LogEnabled, HttpListener
{
    private HttpService m_HttpServer;
    private Logger      m_Logger;
    
    public JsseListener()
    {
    }
    
    /**
     * Enable the logging system.
     *
     * @avalon.logger name="http"
     */
    public void enableLogging( Logger logger )
    {
        m_Logger = logger;
    }
    
    public Logger getLogger()
    {
        return m_Logger;
    }
    
    public void parameterize( Parameters params )
        throws ParameterException
    {
        int reserve = params.getParameterAsInteger( "buffer-reserve", -1 );
        if( reserve > 0 )
            setBufferReserve( reserve );
        
        int size = params.getParameterAsInteger( "buffer-size", -1 );
        if( size > 0 )
            setBufferSize( size );
    
        int confPort = params.getParameterAsInteger( "confidential-port", -1 );
        if( confPort > 0 )
            setConfidentialPort( confPort );
        
        String confScheme = params.getParameter( "confidential-scheme", null );
        if( confScheme != null )
            setConfidentialScheme( confScheme );
    
        String defScheme = params.getParameter( "default-scheme", null );
        if( defScheme != null )
            setDefaultScheme( defScheme );
        
        int integralPort = params.getParameterAsInteger( "integral-port", -1 );
        if( integralPort > 0 )
            setIntegralPort( integralPort );
        
        String integralScheme = params.getParameter( "integral-scheme", null );
        if( integralScheme != null )
            setIntegralScheme( integralScheme );
    
        String host = params.getParameter( "hostname", null );
        try
        {
            if( host != null )
                setHost( host );
        } catch( java.net.UnknownHostException e )
        {
            throw new ParameterException( "Unknown hostname: " + host );
        }
        
        int port = params.getParameterAsInteger( "port", 8443 );
        setPort( port );
        
        int lowResMs = params.getParameterAsInteger( "low-resource-persist-ms", -1 );
        if( lowResMs > 0 )
            setLowResourcePersistTimeMs( lowResMs );
        
        boolean identify = params.getParameterAsBoolean( "identify-listener", false );
        setIdentifyListener( identify );
        
        boolean needClientAuth = params.getParameterAsBoolean( "need-client-authentication", false );
        setNeedClientAuth( needClientAuth );
        
        boolean useDefTrustStore = params.getParameterAsBoolean( "use-default-trust-store", false );
        setUseDefaultTrustStore( useDefTrustStore );
        
        String keyPass = params.getParameter( "key-password", null );
        if( keyPass != null )
            setKeyPassword( keyPass );
            
        String keyStore = params.getParameter( "key-store", null );
        if( keyStore != null )
            setKeystore( keyStore );
            
        String keyStoreProviderClass = params.getParameter( "key-store-provider-class", null );
        if( keyStoreProviderClass != null )
            setKeystoreProviderClass( keyStoreProviderClass );
            
        String keyStoreProviderName = params.getParameter( "key-store-provider-name", null );
        if( keyStoreProviderName != null )
            setKeystoreProviderName( keyStoreProviderName );
            
        String keyStoreType = params.getParameter( "key-store-type", null );
        if( keyStoreType != null )
            setKeystoreType( keyStoreType );
            
        String password = params.getParameter( "password", null );
        if( password != null )
            setPassword( password );
    }
    
    /**
     * @avalon.dependency type="org.apache.avalon.http.HttpService" 
     *                    key="server"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_HttpServer = (HttpService) man.lookup( "server" );
    }
    
    public void start()
        throws Exception
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Starting SSL socket: " + this );
        m_HttpServer.addListener( this );
        super.start();
    }
    
    public void stop()
        throws InterruptedException
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Stopping SSL socket: " + this );
        super.stop();
        m_HttpServer.removeListener( this );
    }
} 
 
