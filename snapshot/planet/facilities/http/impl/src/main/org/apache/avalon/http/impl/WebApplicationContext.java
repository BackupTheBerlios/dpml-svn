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

import java.io.File;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Initializable;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration; 
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.http.HttpContextService;
import org.apache.avalon.http.HttpService;
import org.apache.avalon.http.MimeTypes;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;

/**
 * @avalon.component name="http-web-context" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.http.HttpContextService"
 */
public class WebApplicationContext 
    extends org.mortbay.jetty.servlet.WebApplicationContext
    implements LogEnabled, Contextualizable, Serviceable, Startable, 
               Disposable, Configurable, HttpContextService
{
    private HttpService m_HttpServer;
    private boolean     m_Graceful;
    private Logger      m_Logger;
    
    
    public HttpContext getHttpContext()
    {
        return this;
    }
        
    public Logger getLogger()
    {
        return m_Logger;
    }
    
    /**
     * @avalon.logger name="http"
     */
    public void enableLogging( Logger logger )
    {
        m_Logger = logger;
    }
    
    /** 
     * @avalon.entry key="urn:avalon:temp" 
     *               type="java.io.File"
     * @avalon.entry key="urn:avalon:classloader" 
     *               type="java.lang.ClassLoader"
     */
    public void contextualize( Context ctx )
        throws ContextException
    {
        File tmpDir = (File) ctx.get( "urn:avalon:temp" );
        tmpDir.mkdirs();
        setTempDirectory( tmpDir );
    
        setClassLoaderJava2Compliant( true );
        ClassLoader cl = (ClassLoader) ctx.get( "urn:avalon:classloader" );
        setParentClassLoader( cl );
    }
    
    /**
     * @avalon.dependency type="org.apache.avalon.http.HttpService"
     *                    key="server"
     * @avalon.dependency type="org.apache.avalon.http.MimeTypes"
     *                    key="mimetypes" optional="true"
     * @avalon.dependency type="org.mortbay.http.Authenticator"
     *                    key="authenticator" optional="true"
     * @avalon.dependency type="org.mortbay.http.UserRealm"
     *                    key="realm" optional="true"
     * @avalon.dependency type="org.mortbay.http.RequestLog"
     *                    key="request-log" 
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_HttpServer = (HttpService) man.lookup( "server" );
        
        if( man.hasService( "authenticator" ) )
        {
            Authenticator authenticator = (Authenticator) man.lookup( "authenticator" );
            if( authenticator != null )
                setAuthenticator( authenticator );
        }
        
        if( man.hasService( "realm" ) )
        {
            UserRealm userRealm = (UserRealm) man.lookup( "realm" );
            if( userRealm != null )
            {
                setRealm( userRealm );
                setRealmName( userRealm.getName() ); // Is this necessary?
            }
        
        }
        
        RequestLog requestLog = (RequestLog) man.lookup( "request-log" );
        setRequestLog( requestLog );
        
        if( man.hasService( "mimetypes" ) )
        {
            MimeTypes mimeTypes = (MimeTypes) man.lookup( "mimetypes" );
            if( mimeTypes != null )
                setMimeMap( mimeTypes.getExtensionMap() );
        }
    }

    public void configure( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        boolean ignore = conf.getChild( "ignore-jetty-web" ).getValueAsBoolean( false );
        setIgnoreWebJetty( ignore );
        
        boolean extract = conf.getChild( "extract-war" ).getValueAsBoolean( false );
        setExtractWAR( extract );
        
        String displayName = conf.getChild( "display-name" ).getValue( null );
        if( displayName != null )
            setDisplayName( displayName );
        
        String webApp = conf.getChild( "web-application" ).getValue();
        setWAR( webApp );
        
        String defDescr = conf.getChild( "defaults-descriptor" ).getValue( "etc/webdefaults.xml" );
        setDefaultsDescriptor( defDescr );

        org.apache.avalon.framework.configuration.Configuration errorPageConf = conf.getChild( "error-page" );
        String uri = errorPageConf.getValue( null );
        if( uri != null )
        {
            String error = errorPageConf.getAttribute( "error" );
            setErrorPage( error, uri );
        }
        
        org.apache.avalon.framework.configuration.Configuration virtualHostConf = conf.getChild( "virtual-host" );
        String virtualHost = virtualHostConf.getValue( null );
        addVirtualHost( virtualHost );
                
        org.apache.avalon.framework.configuration.Configuration contextConf = conf.getChild( "context-path" );
        String contextPath = contextConf.getValue( "/" );
        setContextPath( contextPath );
        
        m_Graceful = conf.getChild( "graceful-stop" ).getValueAsBoolean( false );
        
        org.apache.avalon.framework.configuration.Configuration attributes = conf.getChild( "attributes" );
        configureAttributes( attributes );
    
        org.apache.avalon.framework.configuration.Configuration initParams = conf.getChild( "init-parameters" );
        configureInitParameters( initParams );
        
        org.apache.avalon.framework.configuration.Configuration welcomeFiles = conf.getChild( "welcome-files" );
        configureWelcomeFiles( welcomeFiles );
        
        String resourceBase = conf.getChild( "resource-base").getValue( "." );
        File rsrc = new File( resourceBase );
        setResourceBase( rsrc.getAbsolutePath() );
        
        int maxCachedFilesize = conf.getChild( "max-cached-filesize" ).getValueAsInteger( -1 );
        if( maxCachedFilesize > 0 )
            setMaxCachedFileSize( maxCachedFilesize );        
        
        int maxCacheSize = conf.getChild( "max-cache-size" ).getValueAsInteger( -1 );
        if( maxCacheSize > 0 )
            setMaxCacheSize( maxCacheSize );        
    }
    
    private void configureAttributes( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        org.apache.avalon.framework.configuration.Configuration[] children = conf.getChildren( "attribute" );
        for( int i = 0 ; i < children.length ; i++ )
            configureAttribute( children[i] );
    }
    
    private void configureAttribute( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        String name = conf.getAttribute( "name" );
        String value = conf.getValue();
        
        // TODO: setAttribute() support Object as a value.
        //       need to figure out what that could be and introduce
        //       support for it.
        setAttribute( name, value );
    }
    
    private void configureInitParameters( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        org.apache.avalon.framework.configuration.Configuration[] inits = conf.getChildren( "parameter" );
        for( int i=0 ; i < inits.length ; i++ )
        {
            String name = inits[i].getAttribute( "name" );
            String value = inits[i].getAttribute( "value" );
            setInitParameter( name, value );
        }
    }
    
    private void configureVirtualHosts( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        org.apache.avalon.framework.configuration.Configuration[] hosts = conf.getChildren( "host" );
        for( int i=0 ; i < hosts.length ; i++ )
            addVirtualHost( hosts[i].getValue() );
    }
    
    private void configureWelcomeFiles( org.apache.avalon.framework.configuration.Configuration conf )
        throws ConfigurationException
    {
        org.apache.avalon.framework.configuration.Configuration[] files = conf.getChildren( "file" );
        for( int i=0 ; i < files.length ; i++ )
            addWelcomeFile( files[i].getValue() );
    }

    public void start()
        throws Exception
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Starting context: " + this );
        m_HttpServer.addContext( this );
        super.start();
    }
    
    public void stop()
        throws InterruptedException
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Stopping context: " + this );
            
            
        // The following is need due to strange delegation between the 
        // methods stop() and stop( boolean ) in the superclasses are
        // inaccurately implemented, and an endless loop will result
        // unless the isStarted() method is checked.
        if( isStarted() )
            super.stop( m_Graceful );
            
        m_HttpServer.removeContext( this );
    }
    
    public void dispose()
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Disposing context: " + this );
        destroy();
        m_HttpServer = null;
    }
} 
 
