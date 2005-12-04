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

/**
 * HTTP context implementation.
 */
public class HttpContextImpl extends HttpContext
    implements Startable, Disposable, HttpContextService
{
   /**
    * Component deployment context.
    */
    public interface Context
    {
       /**
        * Get the HTTP server.
        * @return the server
        */
        HttpService getServer();
        
       /**
        * Get the temp directory.
        * @return the temp directory
        */
        File getTempDirectory();
        
       /**
        * Get the authenticator.
        * @param value the default authenticator
        * @return the authenticator
        */
        Authenticator getAuthenticator( Authenticator value );
        
       /**
        * Get the user realm.
        * @param value the default user realm
        * @return the user realm
        */
        UserRealm getUserRealm( UserRealm value );
        
       /**
        * Get the mime type registry
        * @param value the default mime types
        * @return the mime types
        */
        MimeTypes getMimeTypes( MimeTypes value );
        
       /**
        * Get the context path
        * @param path the default context path
        * @return the resolved path value
        */
        String getContextPath( String path );
        
       /**
        * Get the resource base.
        * @param base the default base
        * @return the resolved base value
        */
        String getResourceBase( String base );
        
       /**
        * Get the request log.
        * @return the request log
        */
        RequestLog getRequestLog();
        
       /**
        * Get the graceful stop policy.
        * @param flag the default policy
        * @return the graceful stop policy
        */
        boolean getGracefulStop( boolean flag );
        
       /**
        * Get the maximum cached file size value.
        * @param max the component defined default
        * @return the resolved max cached file size value
        */
        int getMaxCachedFileSize( int max );
        
       /**
        * Get the maximum cache size value.
        * @param max the component defined default
        * @return the resolved max cache size value
        */
        int getMaxCacheSize( int max );
    }

    private HttpService m_httpServer;
    private boolean m_graceful;
    private Logger m_logger;

   /**
    * Creation of a new HttpContextImpl instance.
    * @param logger the assigned logging channel
    * @param context the deplooyment context
    * @param conf supplimentary configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public HttpContextImpl( Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;

        File tmpDir = context.getTempDirectory();
        tmpDir.mkdirs();
        setTempDirectory( tmpDir );
        ClassLoader cl = HttpContextImpl.class.getClassLoader();
        setClassLoader( cl );
        m_httpServer = context.getServer();
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

   /**
    * Return the http context instance.
    * @return the context
    */
    public HttpContext getHttpContext()
    {
        return this;
    }

    private void configureAttributes( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "attribute" );
        for( int i=0; i<children.length; i++ )
        {
            configureAttribute( children[i] );
        }
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
        for( int i=0; i<inits.length; i++ )
        {
            String name = inits[i].getAttribute( "name" );
            String value = inits[i].getAttribute( "value" );
            setInitParameter( name, value );
        }
    }

    private void configureVirtualHosts( Configuration conf )
    {
        Configuration[] hosts = conf.getChildren( "host" );
        for( int i=0; i<hosts.length; i++ )
        {
            String host = hosts[i].getValue( null );
            addVirtualHost( host );
        }
    }

    private void configureWelcomeFiles( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] files = conf.getChildren( "file" );
        for( int i=0; i<files.length; i++ )
        {
            addWelcomeFile( files[i].getValue() );
        }
    }

   /**
    * Handle startup.
    * @exception Exception if a startup error ocucrs
    */
    protected void doStart()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting context: " + this );
        }
        m_httpServer.addContext( this );
        super.doStart();
    }

   /**
    * Handle stop.
    * @exception Exception if a shutdown error ocucrs
    */
    protected void doStop()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping context: " + this );
        }
        super.doStop();
        m_httpServer.removeContext( this );
    }

   /**
    * Handle disposal.
    */
    public void dispose()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Disposing context: " + this );
        }
        destroy();
        m_httpServer = null;
    }

   /**
    * Handle a request.
    * @param pathInContext the path
    * @param pathParams the parameters
    * @param request the HTTP request
    * @param response the HTTP response
    * @exception IOException if an I/O error occurs
    * @exception HttpException if an HTTP error occurs
    */
    public void handle( String pathInContext, String pathParams, HttpRequest request, HttpResponse response )
        throws IOException, HttpException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Path: " + pathInContext );
            m_logger.debug( "Parameters: " + pathParams );
            m_logger.debug( "Request:\n" + request );
            HttpHandler[] handlers = getHandlers();
            for( int i=0; i<handlers.length; i++ )
            {
                HttpHandler handler = handlers[i];
                String warning;
                if( handler.isStarted() )
                {
                    warning = "";
                }
                else
                {
                    warning = " :   NOT STARTED.";
                }
                m_logger.debug( "Handler[" + i + "] = " + handler + warning );
            }
        }
        super.handle( pathInContext, pathParams, request, response );
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Response:\n" + response );
        }
    }
}
