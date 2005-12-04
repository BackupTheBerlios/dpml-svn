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
import javax.servlet.ServletContext;

import net.dpml.activity.Disposable;
import net.dpml.activity.Startable;
import net.dpml.configuration.ConfigurationException;
import net.dpml.logging.Logger;

import net.dpml.http.spi.HttpContextService;
import net.dpml.http.spi.HttpService;
import net.dpml.http.spi.MimeTypes;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpContext;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;

/**
 * Jetty WebApplicationContext wrapper.
 */
public class WebApplicationContext
    extends org.mortbay.jetty.servlet.WebApplicationContext
    implements Startable, Disposable, HttpContextService, net.dpml.http.ServletHandler
{
   /**
    * The component deployment context.
    */
    public interface Context
    {
       /**
        * Return the http server.
        * @return the http server
        */
        HttpService getHttpServer();
        
       /**
        * Return the mime types.
        * @param value the default mime types value
        * @return the resolved mime types
        */
        MimeTypes getMimeTypes( MimeTypes value );
        
       /**
        * Return the authenticator.
        * @param value the default authenticator value
        * @return the resolved authenticator
        */
        Authenticator getAuthenticator( Authenticator value );
        
       /**
        * Return the user realm.
        * @param value the default user realm value
        * @return the resolved user realm
        */
        UserRealm getUserRealm( UserRealm value );
        
       /**
        * Return the request log.
        * @param value the default request log value
        * @return the resolved request log
        */
        RequestLog getRequestLog( RequestLog value );
        
       /**
        * Return the temp directory.
        * @return the temp duirectory file
        */
        File getTempDirectory();
    }

    private HttpService m_httpServer;
    private boolean     m_graceful;
    private Logger      m_logger;

   /**
    * Creation of a new WebApplicationContext.
    * @param logger the assigned loging channel
    * @param context the deployment context
    * @param conf supplimental confiuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public WebApplicationContext( Logger logger, Context context, 
       net.dpml.configuration.Configuration conf ) throws ConfigurationException
    {
        m_logger = logger;
        File tmpDir = context.getTempDirectory();
        tmpDir.mkdirs();
        setTempDirectory( tmpDir );
        setClassLoaderJava2Compliant( true );
        ClassLoader cl = WebApplicationContext.class.getClassLoader();
        setParentClassLoader( cl );

        m_httpServer = context.getHttpServer();

        Authenticator authenticator = context.getAuthenticator( null );
        if( authenticator != null )
        {
            setAuthenticator( authenticator );
        }

        UserRealm userRealm = context.getUserRealm( null );
        if( userRealm != null )
        {
            setRealm( userRealm );
            setRealmName( userRealm.getName() ); // Is this necessary?
        }

        RequestLog requestLog = context.getRequestLog( null );
        if( requestLog != null )
        {
            setRequestLog( requestLog );
        }

        MimeTypes mimeTypes = context.getMimeTypes( null );
        if( mimeTypes != null )
        {
            setMimeMap( mimeTypes.getExtensionMap() );
        }

        boolean ignore = conf.getChild( "ignore-jetty-web" ).getValueAsBoolean( false );
        setIgnoreWebJetty( ignore );

        boolean extract = conf.getChild( "extract-war" ).getValueAsBoolean( false );
        setExtractWAR( extract );

        String displayName = conf.getChild( "display-name" ).getValue( null );
        if( displayName != null )
        {
            setDisplayName( displayName );
        }

        String webApp = conf.getChild( "web-application" ).getValue();
        setWAR( webApp );

        String defDescr = conf.getChild( "defaults-descriptor" ).getValue( "etc/webdefaults.xml" );
        setDefaultsDescriptor( defDescr );

        net.dpml.configuration.Configuration errorPageConf = conf.getChild( "error-page" );
        String uri = errorPageConf.getValue( null );
        if( uri != null )
        {
            String error = errorPageConf.getAttribute( "error" );
            setErrorPage( error, uri );
        }

        net.dpml.configuration.Configuration virtualHostConf = conf.getChild( "virtual-hosts" );
        configureVirtualHosts( virtualHostConf );

        net.dpml.configuration.Configuration contextConf = conf.getChild( "context-path" );
        String contextPath = contextConf.getValue( "/" );
        setContextPath( contextPath );

        m_graceful = conf.getChild( "graceful-stop" ).getValueAsBoolean( false );

        net.dpml.configuration.Configuration attributes = conf.getChild( "attributes" );
        configureAttributes( attributes );

        net.dpml.configuration.Configuration initParams = conf.getChild( "init-parameters" );
        configureInitParameters( initParams );

        net.dpml.configuration.Configuration welcomeFiles = conf.getChild( "welcome-files" );
        configureWelcomeFiles( welcomeFiles );

        String resourceBase = conf.getChild( "resource-base" ).getValue( "." );
        setResourceBase( resourceBase );

        int maxCachedFilesize = conf.getChild( "max-cached-filesize" ).getValueAsInteger( -1 );
        if( maxCachedFilesize > 0 )
        {
            setMaxCachedFileSize( maxCachedFilesize );
        }

        int maxCacheSize = conf.getChild( "max-cache-size" ).getValueAsInteger( -1 );
        if( maxCacheSize > 0 )
        {
            setMaxCacheSize( maxCacheSize );
        }
    }

   /**
    * Return the http context.
    * @return the http context
    */
    public HttpContext getHttpContext()
    {
        return this;
    }

    private void configureAttributes( net.dpml.configuration.Configuration conf )
        throws ConfigurationException
    {
        net.dpml.configuration.Configuration[] children = conf.getChildren( "attribute" );
        for( int i=0; i<children.length; i++ )
        {
            configureAttribute( children[i] );
        }
    }

    private void configureAttribute( net.dpml.configuration.Configuration conf )
        throws ConfigurationException
    {
        String name = conf.getAttribute( "name" );
        String value = conf.getValue();

        // TODO: setAttribute() support Object as a value.
        //       need to figure out what that could be and introduce
        //       support for it.
        setAttribute( name, value );
    }

    private void configureInitParameters( net.dpml.configuration.Configuration conf )
        throws ConfigurationException
    {
        net.dpml.configuration.Configuration[] inits = conf.getChildren( "parameter" );
        for( int i=0; i<inits.length; i++ )
        {
            String name = inits[i].getAttribute( "name" );
            String value = inits[i].getAttribute( "value" );
            setInitParameter( name, value );
        }
    }

    private void configureVirtualHosts( net.dpml.configuration.Configuration conf )
        throws ConfigurationException
    {
        net.dpml.configuration.Configuration[] hosts = conf.getChildren( "host" );
        for( int i=0; i<hosts.length; i++ )
        {
            String host = hosts[i].getValue();
            addVirtualHost( host );
        }
    }

    private void configureWelcomeFiles( net.dpml.configuration.Configuration conf )
        throws ConfigurationException
    {
        net.dpml.configuration.Configuration[] files = conf.getChildren( "file" );
        for( int i=0; i<files.length; i++ )
        {
            addWelcomeFile( files[i].getValue() );
        }
    }

   /**
    * Start the handler.
    * @exception Exception if an error occurs
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
    * Stop the handler.
    * @exception Exception if an error occurs
    */
    protected void doStop()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping context: " + this );
        }
        m_httpServer.removeContext( this );
        super.doStop();
    }

   /**
    * Dispose of the handler.
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
    * Adds the contextObject into the ServletContext object.
    * @param entryName the entry name
    * @param contextObject the context object
    */
    public void addServletContextEntry( String entryName, Object contextObject )
    {
        ServletContext ctx = getServletHandler().getServletContext();
        ctx.setAttribute( entryName, contextObject );
    }
}

