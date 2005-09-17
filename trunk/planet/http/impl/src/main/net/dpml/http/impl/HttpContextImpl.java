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

import java.io.File;
import java.io.IOException;

import net.dpml.activity.Disposable;
import net.dpml.activity.Startable;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import net.dpml.logging.Logger;

import net.dpml.http.spi.HttpContextService;
import net.dpml.http.spi.HttpService;
import net.dpml.http.spi.MimeTypes;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;

public class HttpContextImpl extends HttpContext
    implements Startable, Disposable, HttpContextService
{
    public interface Context
    {
        HttpService getServer();
        File getTempDirectory();
        Authenticator getAuthenticator( Authenticator value );
        UserRealm getUserRealm( UserRealm value );
        MimeTypes getMimeTypes( MimeTypes value );
        String getContextPath( String path );
        String getResourceBase( String base );
        RequestLog getRequestLog();
        boolean getGracefulStop( boolean flag );
        int getMaxCachedFileSize( int max );
        int getMaxCacheSize( int max );
    }

    private HttpService m_HttpServer;
    private boolean     m_graceful;
    private Logger      m_logger;

    public HttpContextImpl( Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;

        File tmpDir = context.getTempDirectory();
        tmpDir.mkdirs();
        setTempDirectory( tmpDir );
        ClassLoader cl = HttpContextImpl.class.getClassLoader();
        setClassLoader( cl );
        m_HttpServer = context.getServer();
        Authenticator authenticator = context.getAuthenticator( null );
        if( null != authenticator )
        {
            setAuthenticator( authenticator );
        }
        UserRealm userRealm = context.getUserRealm( null );
        if( null != userRealm )
        {
            setRealm( userRealm );
            setRealmName( userRealm.getName() ); // this IS necessary.
        }
        RequestLog requestLog = context.getRequestLog();
        setRequestLog( requestLog );
        MimeTypes mimeTypes = context.getMimeTypes( null );
        if( null != mimeTypes )
        {
            setMimeMap( mimeTypes.getExtensionMap() );
        }

        Configuration virtualHostConf = conf.getChild( "virtual-hosts" );
        configureVirtualHosts( virtualHostConf );

        m_graceful = context.getGracefulStop( false );
        String contextPath = context.getContextPath( "/" );
        setContextPath( contextPath );
        String resourceBase = context.getResourceBase( "." );
        setResourceBase( resourceBase );

        Configuration attributes = conf.getChild( "attributes" );
        configureAttributes( attributes );

        Configuration initParams = conf.getChild( "init-parameters" );
        configureInitParameters( initParams );

        Configuration welcomeFiles = conf.getChild( "welcome-files" );
        configureWelcomeFiles( welcomeFiles );

        int maxCachedFilesize = context.getMaxCachedFileSize( -1 );
        if( maxCachedFilesize > 0 )
        {
            setMaxCachedFileSize( maxCachedFilesize );
        }

        int maxCacheSize = context.getMaxCacheSize( -1 );
        if( maxCacheSize > 0 )
        {
            setMaxCacheSize( maxCacheSize );
        }
    }

    public HttpContext getHttpContext()
    {
        return this;
    }

    private void configureAttributes( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "attribute" );
        for( int i = 0 ; i < children.length ; i++ )
            configureAttribute( children[i] );
    }

    private void configureAttribute( Configuration conf )
        throws ConfigurationException
    {
        String name = conf.getAttribute( "name" );
        String value = conf.getValue();

        // TODO: setAttribute() support Object as a value.
        //       need to figure out what that could be and introduce
        //       support for it.
        setAttribute( name, value );
    }

    private void configureInitParameters( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] inits = conf.getChildren( "parameter" );
        for( int i=0 ; i < inits.length ; i++ )
        {
            String name = inits[i].getAttribute( "name" );
            String value = inits[i].getAttribute( "value" );
            setInitParameter( name, value );
        }
    }

    private void configureVirtualHosts( Configuration conf )
    {
        Configuration[] hosts = conf.getChildren( "host" );
        for( int i=0 ; i < hosts.length ; i++ )
        {
            String host = hosts[i].getValue( null );
            addVirtualHost( host );
        }
    }

    private void configureWelcomeFiles( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] files = conf.getChildren( "file" );
        for( int i=0 ; i < files.length ; i++ )
        {
            addWelcomeFile( files[i].getValue() );
        }
    }

    protected void doStart()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
            m_logger.debug( "Starting context: " + this );
        m_HttpServer.addContext( this );
        super.doStart();
    }

    protected void doStop()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
            m_logger.debug( "Stopping context: " + this );
        super.doStop();
        m_HttpServer.removeContext( this );
    }

    public void dispose()
    {
        if( m_logger.isDebugEnabled() )
            m_logger.debug( "Disposing context: " + this );
        destroy();
        m_HttpServer = null;
    }

    public void handle( String pathInContext, String pathParams, HttpRequest request, HttpResponse response)
        throws IOException, HttpException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Path: " + pathInContext );
            m_logger.debug( "Parameters: " + pathParams );
            m_logger.debug( "Request:\n" + request );
            HttpHandler[] handlers = getHandlers();
            for( int i=0 ; i < handlers.length ; i++ )
            {
                HttpHandler handler = handlers[i];
                String warning;
                if( handler.isStarted() )
                    warning = "";
                else
                    warning = " :   NOT STARTED.";
                m_logger.debug( "Handler[" + i + "] = " + handler + warning );
            }
        }
        super.handle( pathInContext, pathParams, request, response );
        if( m_logger.isDebugEnabled() )
            m_logger.debug( "Response:\n" + response );
    }
}
