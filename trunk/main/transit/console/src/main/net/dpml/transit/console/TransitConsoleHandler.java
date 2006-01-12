/*
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.transit.console;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.util.HelpFormatter;
import net.dpml.cli.OptionException;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.URLValidator;
import net.dpml.cli.validation.NumberValidator;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Transit Plugin that provides support for the configuration of the Transit subsystem.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitConsoleHandler 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    private final TransitDirective m_directive;
    private final CommandLine m_line;
    private final TransitDirectiveBuilder m_builder;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new Transit CLI handler.
    * 
    * @param logger the assigned logging channel
    * @param args command line arguments
    * @exception Exception if an error occurs during plugin establishment
    */
    public TransitConsoleHandler( Logger logger, String[] args ) throws Exception
    {
        this( logger, getCommandLine( logger, args ) );
    }
    
   /**
    * Creation of a new Transit CLI handler.
    *
    * @param logger the assigned logging channel
    * @param args command line arguments
    * @exception Exception if an error occurs during plugin establishment
    */
    private TransitConsoleHandler( final Logger logger, final CommandLine line ) throws Exception
    {
        if( null == line ) 
        {
            m_line = null;
            m_directive = null;
            m_builder = null;
            m_logger = null;
            System.exit( -1 );
        }
        else
        {
            m_logger = logger;
            m_line = line;
            m_directive = loadTransitDirective( line );
            m_builder = new TransitDirectiveBuilder( m_directive );
            if( null == m_directive )
            {
                System.exit( -1 );
            }
        }
        
        // handle command
        
        if( line.hasOption( INFO_COMMAND ) )
        {
            processInfo( line );
        }
        else if( line.hasOption( ADD_COMMAND ) )
        {
            TransitDirective directive = processAdd( line );
            export( directive );
        }
        else if( line.hasOption( SET_COMMAND ) )
        {
            TransitDirective directive = processSet( line );
            export( directive );
        }
        else if( line.hasOption( REMOVE_COMMAND ) )
        {
            TransitDirective directive = processRemove( line );
            export( directive );
        }
        else if( line.hasOption( REVERT_COMMAND ) )
        {
            export( TransitDirective.CLASSIC_PROFILE );
        }
        else
        {
            processHelp();
        }
    }
    
    // ------------------------------------------------------------------------
    // command handling
    // ------------------------------------------------------------------------
    
    private void processInfo( CommandLine line )
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( "\n\n  Version " + Transit.VERSION );
        
        buffer.append( "\n\n  Environment" );
        buffer.append( "\n\n    ${dpml.home} \t" + Transit.DPML_HOME );
        buffer.append( "\n    ${dpml.data} \t" + Transit.DPML_DATA );
        buffer.append( "\n    ${dpml.prefs} \t" + Transit.DPML_PREFS );
        buffer.append( "\n    ${dpml.system} \t" + Transit.DPML_SYSTEM );
        
        ProxyDirective proxy = m_directive.getProxyDirective();
        if( null != proxy )
        {
            buffer.append( "\n\n  Proxy Settings" );
            buffer.append( "\n\n    Host: \t" + proxy.getHost() );
            if( proxy.getUsername() != null )
            {
                buffer.append( "\n    Username: \t" + proxy.getUsername() );
            }
            char[] pswd = proxy.getPassword();
            if( null != pswd )
            {
                buffer.append( "\n    Password: \t" );
                for( int i=0; i<pswd.length; i++ )
                {
                    buffer.append( "*" );
                }
            }
            String[] excludes = proxy.getExcludes();
            if( excludes.length > 0 )
            {
                buffer.append( "\n    Excludes: \t" );
                for( int i=0; i<excludes.length; i++ )
                {
                    String exclude = excludes[i];
                    buffer.append( exclude );
                    if( i < ( excludes.length-1 ) )
                    {
                        buffer.append( ", " );
                    }
                }
            }
        }
        
        CacheDirective cache = m_directive.getCacheDirective();
        
        buffer.append( "\n\n  Cache and System Settings" );
        buffer.append( "\n\n    Cache Directory: \t" + cache.getCache() );
        buffer.append( "\n    Cache Layout: \t" + cache.getCacheLayout() );
        buffer.append( "\n    System Directory: \t" + cache.getLocal() );
        buffer.append( "\n    Repository Layout: \t" + cache.getLocalLayout() );
        
        HostDirective[] hosts = cache.getHostDirectives();
        if( hosts.length > 0 )
        {
            buffer.append( "\n\n  Host Settings" );
            for( int i=0; i<hosts.length; i++ )
            {
                HostDirective host = hosts[i];
                buffer.append( "\n\n    " + host.getID() + " (" + ( i+1 ) + ")" );
                buffer.append( "\n\n      URL\t" + host.getHost() );
                buffer.append( "\n      Priority:\t" + host.getPriority() );
                if( host.getIndex() != null )
                {
                    buffer.append( "\n      Index: \t" + host.getIndex() );
                }
                buffer.append( "\n      Enabled: \t" + host.getEnabled() );
                buffer.append( "\n      Trusted: \t" + host.getTrusted() );
                buffer.append( "\n      Layout: \t" + host.getLayout() );
                if( host.getUsername() != null )
                {
                    buffer.append( "\n      Username: \t" + host.getUsername() );
                }
                buffer.append( "\n      Password: \t" );
                char[] pswd = host.getPassword();
                if( null != pswd )
                {
                    for( int j=0; j<pswd.length; j++ )
                    {
                        buffer.append( "*" );
                    }
                }
                buffer.append( "\n      Prompt: \t" + host.getPrompt() );
                buffer.append( "\n      Scheme: \t" + host.getScheme() );
            }
        }
        else
        {
            buffer.append( "\n\n  No hosts." );
        }
        
        LayoutDirective[] layouts = cache.getLayoutDirectives();
        if( layouts.length > 0 )
        {
            buffer.append( "\n\n  Layout Settings" );
            for( int i=0; i<layouts.length; i++ )
            {
                LayoutDirective layout = layouts[i];
                buffer.append( "\n\n    " + layout.getID() + " (" + ( i+1 ) + ")" );
                buffer.append( "\n\n      Codebase:\t" + layout.getCodeBaseURI() );
                buffer.append( "\n      Title:\t" + layout.getTitle() );
            }
        }
        
        ContentDirective[] handlers = cache.getContentDirectives();
        if( handlers.length > 0 )
        {
            buffer.append( "\n\n  Content Handler Settings" );
            for( int i=0; i<handlers.length; i++ )
            {
                ContentDirective handler = handlers[i];
                buffer.append( "\n\n    " + handler.getID() + " (" + ( i+1 ) + ")" );
                buffer.append( "\n\n      Codebase:\t" + handler.getCodeBaseURI() );
                buffer.append( "\n      Title:\t" + handler.getTitle() );
            }
        }
        
        System.out.println( buffer.toString() );
    }
    
    private TransitDirective processAdd( CommandLine line ) throws Exception
    {
        if( line.hasOption( ADD_HOST_COMMAND ) )
        {
            String key = (String) line.getValue( ADD_HOST_COMMAND, null );
            CacheDirective cache = m_directive.getCacheDirective();
            HostDirective[] hosts = cache.getHostDirectives();
            for( int i=0; i<hosts.length; i++ )
            {
                HostDirective h = hosts[i];
                if( h.getID().equals( key ) )
                {
                    System.out.println( "ERROR: Host id '" + key + "' already assigned." );
                    return null;
                }
            }
            
            System.out.println( "Adding host: " + key );
            URL url = (URL) line.getValue( REQUIRED_HOST_OPTION, null );
            String username = (String) line.getValue( USERNAME_OPTION, null );
            String password = (String) line.getValue( PASSWORD_OPTION, null );
            Number priority = (Number) line.getValue( HOST_PRIORITY_OPTION, new Integer( 100 ) );
            String index = (String) line.getValue( HOST_INDEX_OPTION, null );
            boolean enabled = !line.hasOption( DISABLED_OPTION );
            boolean trusted = line.hasOption( TRUSTED_OPTION );
            String layout = (String) line.getValue( LAYOUT_OPTION, "classic" );
            String scheme = (String) line.getValue( HOST_SCHEME_OPTION, "" );
            String prompt = (String) line.getValue( HOST_PROMPT_OPTION, "" );
            
            HostDirective host = 
              new HostDirective( 
                key, priority.intValue(), url.toString(), index, username, 
                toCharArray( password ), enabled, trusted, layout, 
                scheme, prompt );
                
            HostDirective[] newHosts = new HostDirective[ hosts.length + 1 ];
            for( int i=0; i<hosts.length; i++ )
            {
                newHosts[i] = hosts[i];
            }
            newHosts[ hosts.length ] = host;
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            CacheDirective newCache = builder.create( newHosts );
            return m_builder.create( newCache );
        }
        else if( line.hasOption( ADD_HANDLER_COMMAND ) )
        {
            String key = (String) line.getValue( ADD_HANDLER_COMMAND, null );
            CacheDirective cache = m_directive.getCacheDirective();
            ContentDirective[] handlers = cache.getContentDirectives();
            for( int i=0; i<handlers.length; i++ )
            {
                ContentDirective c = handlers[i];
                if( c.getID().equals( key ) )
                {
                    System.out.println( "ERROR: Content handler id '" + key + "' already assigned." );
                    return null;
                }
            }
            
            System.out.println( "Adding content handler: " + key );
            URI uri = (URI) line.getValue( REQUIRED_CODEBASE_OPTION, null );
            String title = (String) line.getValue( TITLE_OPTION, null );
            
            ContentDirective handler = 
              new ContentDirective( 
                key, title, uri.toASCIIString(), new ValueDirective[0] );
            
            ContentDirective[] newHandlers = new ContentDirective[ handlers.length + 1 ];
            for( int i=0; i<handlers.length; i++ )
            {
                newHandlers[i] = handlers[i];
            }
            newHandlers[ handlers.length ] = handler;
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            CacheDirective newCache = builder.create( newHandlers );
            return m_builder.create( newCache );
        }
        else if( line.hasOption( ADD_LAYOUT_COMMAND ) )
        {
            String key = (String) line.getValue( ADD_LAYOUT_COMMAND, null );
            CacheDirective cache = m_directive.getCacheDirective();
            LayoutDirective[] handlers = cache.getLayoutDirectives();
            for( int i=0; i<handlers.length; i++ )
            {
                LayoutDirective c = handlers[i];
                if( c.getID().equals( key ) )
                {
                    System.out.println( "ERROR: Layout id '" + key + "' already assigned." );
                    return null;
                }
            }
            
            System.out.println( "Adding layout: " + key );
            URI uri = (URI) line.getValue( REQUIRED_CODEBASE_OPTION, null );
            String title = (String) line.getValue( TITLE_OPTION, null );
            
            LayoutDirective handler = 
              new LayoutDirective( 
                key, title, uri.toASCIIString(), new ValueDirective[0] );
            
            LayoutDirective[] newHandlers = new LayoutDirective[ handlers.length + 1 ];
            for( int i=0; i<handlers.length; i++ )
            {
                newHandlers[i] = handlers[i];
            }
            newHandlers[ handlers.length ] = handler;
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            CacheDirective newCache = builder.create( newHandlers );
            return m_builder.create( newCache );
        }
        else
        {
            throw new IllegalStateException( "Unqualified add." );
        }
    }
    
    private TransitDirective processSet( CommandLine line ) throws Exception
    {
        if( line.hasOption( SET_CACHE_COMMAND ) )
        {
            return setCache( line );
        }
        else if( line.hasOption( SET_SYSTEM_COMMAND ) )
        {
            return setSystem( line );
        }
        else if( line.hasOption( PROXY_COMMAND ) )
        {
            return setProxy( line );
        }
        else if( line.hasOption( SET_HOST_COMMAND ) )
        {
            return setHost( line );
        }
        else if( line.hasOption( SET_HANDLER_COMMAND ) )
        {
            return setHandler( line );
        }
        else if( line.hasOption( SET_LAYOUT_COMMAND ) )
        {
            return setLayout( line );
        }
        else
        {
            throw new IllegalStateException( "Unqualified set command." );
        }
    }
    
    private TransitDirective setCache( CommandLine line ) throws Exception
    {
        String cache = (String) line.getValue( DIRECTORY_OPTION );
        String layout = (String) line.getValue( LAYOUT_OPTION );
        CacheDirective directive = m_directive.getCacheDirective();
        CacheDirectiveBuilder builder = new CacheDirectiveBuilder( directive );
        CacheDirective newCache = builder.create( cache, layout, null, null );
        return m_builder.create( newCache );
    }
    
    private TransitDirective setSystem( CommandLine line ) throws Exception
    {
        String system = (String) line.getValue( DIRECTORY_OPTION );
        String layout = (String) line.getValue( LAYOUT_OPTION );
        CacheDirective directive = m_directive.getCacheDirective();
        CacheDirectiveBuilder builder = new CacheDirectiveBuilder( directive );
        CacheDirective newCache = builder.create( null, null, system, layout );
        return m_builder.create( newCache );
    }
    
    private TransitDirective setProxy( CommandLine line ) throws Exception
    {
        System.out.println( "Updating proxy configuration." );
        ProxyDirective proxy = m_directive.getProxyDirective();
        
        URL url = (URL) line.getValue( HOST_OPTION, null );
        String username = (String) line.getValue( USERNAME_OPTION, null );
        String password = (String) line.getValue( PASSWORD_OPTION, null );
        List values = line.getValues( PROXY_EXCLUDE_OPTION );
        String[] excludes = (String[]) values.toArray( new String[0] );
        char[] pswd = toCharArray( password );
        if( null == proxy )
        {
            if( null == url )
            {
                System.out.println( "ERROR: Missing proxy host option." );
                return null;
            }
            ProxyDirective p = new ProxyDirective( 
              url.toString(), excludes, username, pswd );
            return m_builder.create( p );
        }
        else
        {
            ProxyDirectiveBuilder builder = new ProxyDirectiveBuilder( proxy );
            ProxyDirective p = builder.create( url, excludes, username, pswd );
            return m_builder.create( p );
        }
    }
    
    private TransitDirective setHost( CommandLine line ) throws Exception
    {
        String key = (String) line.getValue( SET_HOST_COMMAND, null );
        CacheDirective cache = m_directive.getCacheDirective();
        HostDirective[] hosts = cache.getHostDirectives();
        HostDirective host = null;
        for( int i=0; i<hosts.length; i++ )
        {
            HostDirective h = hosts[i];
            if( h.getID().equals( key ) )
            {
                host = h;
            }
        }
        
        if( null == host )
        {
            System.out.println( "ERROR: Host id '" + key + "' not recognized." );
            return null;
        }
        
        System.out.println( "Updating host: " + key );
        URL url = (URL) line.getValue( HOST_OPTION, null );
        int priority = getPriorityValue( line );
        String index = (String) line.getValue( HOST_INDEX_OPTION, null );
        String username = (String) line.getValue( USERNAME_OPTION, null );
        String password = (String) line.getValue( PASSWORD_OPTION, null );
        boolean enabled = getEnabledFlag( line, host );
        boolean trusted = getTrustedFlag( line, host );
        String layout = (String) line.getValue( LAYOUT_OPTION, null );
        String scheme = (String) line.getValue( HOST_SCHEME_OPTION, null );
        String prompt = (String) line.getValue( HOST_PROMPT_OPTION, null );
        
        HostDirectiveBuilder builder = new HostDirectiveBuilder( host );
        HostDirective newHost = 
          builder.create( 
            priority, url, index, username, toCharArray( password ),
            enabled, trusted, layout, scheme, prompt );
        
        HostDirective[] newHosts = new HostDirective[ hosts.length ];
        for( int i=0; i<hosts.length; i++ )
        {   
            HostDirective h = hosts[i];
            if( h.getID().equals( key ) )
            {
                newHosts[i] = newHost;
            }
            else
            {
                newHosts[i] = h;
            }
        }
        CacheDirectiveBuilder cacheBuilder = new CacheDirectiveBuilder( cache );
        CacheDirective newCache = cacheBuilder.create( newHosts );
        return m_builder.create( newCache );
    }
    
    private TransitDirective setHandler( CommandLine line ) throws Exception
    {
        String key = (String) line.getValue( SET_HANDLER_COMMAND, null );
        CacheDirective cache = m_directive.getCacheDirective();
        ContentDirective[] handlers = cache.getContentDirectives();
        ContentDirective handler = null;
        for( int i=0; i<handlers.length; i++ )
        {
            ContentDirective c = handlers[i];
            if( c.getID().equals( key ) )
            {
                handler = c;
            }
        }

        if( null == handler )
        {
            System.out.println( "ERROR: Content handler id '" + key + "' not recognized." );
            return null;
        }
            
        System.out.println( "Updating content handler: " + key );
        URI uri = (URI) line.getValue( CODEBASE_OPTION, null );
        String title = (String) line.getValue( TITLE_OPTION, null );
        
        ContentDirectiveBuilder builder = new ContentDirectiveBuilder( handler );
        ContentDirective newDirective = 
          builder.create( title, uri, null );
                
            
        ContentDirective[] newDirectives = new ContentDirective[ handlers.length ];
        for( int i=0; i<handlers.length; i++ )
        {   
            ContentDirective d = handlers[i];
            if( d.getID().equals( key ) )
            {
                newDirectives[i] = newDirective;
            }
            else
            {
               newDirectives[i] = d;
            }
        }
        CacheDirectiveBuilder cacheBuilder = new CacheDirectiveBuilder( cache );
        CacheDirective newCache = cacheBuilder.create( newDirectives );
        return m_builder.create( newCache );    
    }
    
    private TransitDirective setLayout( CommandLine line ) throws Exception
    {
        String key = (String) line.getValue( SET_LAYOUT_COMMAND, null );
        CacheDirective cache = m_directive.getCacheDirective();
        LayoutDirective[] handlers = cache.getLayoutDirectives();
        LayoutDirective handler = null;
        for( int i=0; i<handlers.length; i++ )
        {
            LayoutDirective c = handlers[i];
            if( c.getID().equals( key ) )
            {
                handler = c;
            }
        }

        if( null == handler )
        {
            System.out.println( "ERROR: Layout id '" + key + "' not recognized." );
            return null;
        }
            
        System.out.println( "Updating layout: " + key );
        URI uri = (URI) line.getValue( CODEBASE_OPTION, null );
        String title = (String) line.getValue( TITLE_OPTION, null );
            
        LayoutDirectiveBuilder builder = new LayoutDirectiveBuilder( handler );
        LayoutDirective newDirective = 
          builder.create( title, uri, null );
            
        LayoutDirective[] newDirectives = new LayoutDirective[ handlers.length ];
        for( int i=0; i<handlers.length; i++ )
        {   
            LayoutDirective d = handlers[i];
            if( d.getID().equals( key ) )
            {
                newDirectives[i] = newDirective;
            }
            else
            {
                newDirectives[i] = d;
            }
        }
        CacheDirectiveBuilder cacheBuilder = new CacheDirectiveBuilder( cache );
        CacheDirective newCache = cacheBuilder.create( newDirectives );
        return m_builder.create( newCache );
    }
    
    private TransitDirective processRemove( CommandLine line )
    {
        if( line.hasOption( SELECT_HOST_COMMAND ) )
        {
            String key = (String) line.getValue( SELECT_HOST_COMMAND, null );
            System.out.println( "Removing resource host: " + key );
            CacheDirective cache = m_directive.getCacheDirective();
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            try
            {
                CacheDirective newCache = builder.removeHostDirective( key );
                return m_builder.create( newCache );
            }
            catch( UnknownKeyException e )
            {
                System.out.println( "Unknown host '" + key + "'." );
                return null;
            }
        }
        else if( line.hasOption( SELECT_HANDLER_COMMAND ) )
        {
            String key = (String) line.getValue( SELECT_HANDLER_COMMAND, null );
            System.out.println( "Removing content handler: " + key );
            CacheDirective cache = m_directive.getCacheDirective();
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            try
            {
                CacheDirective newCache = builder.removeContentDirective( key );
                return m_builder.create( newCache );
            }
            catch( UnknownKeyException e )
            {
                System.out.println( "Unknown content handler '" + key + "'." );
                return null;
            }
        }
        else if( line.hasOption( SELECT_LAYOUT_COMMAND ) )
        {
            String key = (String) line.getValue( SELECT_LAYOUT_COMMAND, null );
            System.out.println( "Removing layout: " + key );
            CacheDirective cache = m_directive.getCacheDirective();
            CacheDirectiveBuilder builder = new CacheDirectiveBuilder( cache );
            try
            {
                CacheDirective newCache = builder.removeLayoutDirective( key );
                return m_builder.create( newCache );
            }
            catch( UnknownKeyException e )
            {
                System.out.println( "Unknown layout '" + key + "'." );
                return null;
            }
        }
        else if( line.hasOption( REMOVE_PROXY_COMMAND ) )
        {
            if( null == m_directive.getProxyDirective() )
            {
                System.out.println( "Nothing to remove." );
                return null;
            }
            else
            {
                System.out.println( "Removing all proxy settings." );
                CacheDirective cache = m_directive.getCacheDirective();
                return new TransitDirective( null, cache );
            }
        }
        else
        {
            throw new IllegalStateException( "Unqualified remove command." );
        }
    }
    
    private void export( TransitDirective directive )
    {
        if( null == directive )
        {
            return;
        }
        else if( m_directive.equals( directive ) )
        {
            System.out.println( "# no change" );
        }
        else
        {
            URI store = DefaultTransitModel.DEFAULT_PROFILE_URI;
            URI uri = (URI) m_line.getValue( PROFILE_URI_OPTION, store );
            System.out.println( "Saving to: " + uri );
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( TransitDirective.class.getClassLoader() );
            XMLEncoder encoder = null;
            try
            {
                URL url = uri.toURL();
                OutputStream output = url.openConnection().getOutputStream();
                BufferedOutputStream buffer = new BufferedOutputStream( output );
                encoder = new XMLEncoder( buffer );
                encoder.writeObject( directive );
            }
            catch( Exception e )
            {
                final String error = 
                  "Internal error while attempting to write to the uri: "
                  + uri;
                getLogger().error( error, e );
            }
            finally
            {
                if( null != encoder )
                {
                    encoder.close();
                }
                Thread.currentThread().setContextClassLoader( current );
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // internal utilities
    // ------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private char[] toCharArray( String value )
    {
        if( null == value )
        {
            return null;
        }
        else
        {
            return value.toCharArray();
        }
    }
    
    private TransitDirective loadTransitDirective( CommandLine line )
    {
        if( line.hasOption( PROFILE_URI_OPTION ) )
        {
            URI uri = (URI) line.getValue( PROFILE_URI_OPTION, null );
            try
            {
                URL url = uri.toURL();
                InputStream input = url.openStream();
                XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
                return (TransitDirective) decoder.readObject();
            }
            catch( FileNotFoundException e )
            {
                final String error = 
                  "Resource not found: "
                + uri;
                getLogger().warn( error );
                return null;
            }
            catch( Exception e )
            {
                final String error = 
                  "An error occured while attempting to load the transit profile: "
                  + uri;
                getLogger().error( error, e  );
                return null;
            }
        }
        else
        {
            File prefs = Transit.DPML_PREFS;
            File config = new File( prefs, "dpml/transit/xmls/config.xml" );
            if( config.exists() )
            {
                try
                {
                    FileInputStream input = new FileInputStream( config );
                    XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
                    return (TransitDirective) decoder.readObject();
                }
                catch( Exception e )
                {
                    final String error = 
                      "An error occured while attempting to load the transit profile: "
                    + config;
                    getLogger().error( error, e  );
                    return null;
                }
            }
            else
            {
                //
                // load default profile
                //
                
                return TransitDirective.CLASSIC_PROFILE;
            }
        }
    }
    
    private boolean getEnabledFlag( CommandLine line, HostDirective host )
    {
        if( line.hasOption( DISABLED_OPTION ) )
        {
            return false;
        }
        else if( line.hasOption( ENABLED_OPTION ) )
        {
            return true;
        }
        else
        {
            return host.getEnabled();
        }
    }
    
    private boolean getTrustedFlag( CommandLine line, HostDirective host )
    {
        if( line.hasOption( TRUSTED_OPTION ) )
        {
            return true;
        }
        else if( line.hasOption( UNTRUSTED_OPTION ) )
        {
            return false;
        }
        else
        {
            return host.getTrusted();
        }
    }
    
    private int getPriorityValue( CommandLine line )
    {
        if( line.hasOption( HOST_PRIORITY_OPTION ) )
        {
            Number priority = (Number) line.getValue( HOST_PRIORITY_OPTION, new Integer( -1 ) );
            return priority.intValue();
        }
        else
        {
            return -1;
        }
    }
    
    // ------------------------------------------------------------------------
    // static utilities
    // ------------------------------------------------------------------------
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    private static final NumberValidator INTERGER_VALIDATOR = NumberValidator.getIntegerInstance();
    private static final Set HELP_TOPICS = new HashSet();
    
    static
    {
        HELP_TOPICS.add( "add" );
        HELP_TOPICS.add( "set" );
        HELP_TOPICS.add( "remove" );
    }
    
    private static final Option DIRECTORY_OPTION = 
        OPTION_BUILDER
          .withShortName( "dir" )
          .withDescription( "Directory." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Directory." )
              .withName( "path" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .create() )
          .create();
    
    private static final Option SYSTEM_LIBRARY_OPTION = 
        OPTION_BUILDER
          .withShortName( "system" )
          .withDescription( "Local system repository." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Directory." )
              .withName( "dir" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .create() )
          .create();
          
    private static final Option PROFILE_URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "profile" )
          .withDescription( "Configuration profile uri." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "URI path." )
              .withName( "uri" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
    
    private static final Option HELP_COMMAND =
      COMMAND_BUILDER
        .withName( "help" )
        .withDescription( "Print command help." )
        .create();
        
    private static final Option HOST_OPTION = 
      OPTION_BUILDER
        .withShortName( "url" )
        .withDescription( "Host URL." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "url" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new URLValidator() )
            .create() )
        .create();
    
    private static final Option REQUIRED_HOST_OPTION = 
      OPTION_BUILDER
        .withShortName( "url" )
        .withDescription( "Host URL (required)." )
        .withRequired( true )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "url" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new URLValidator() )
            .create() )
        .create();
    
    private static final Option REQUIRED_CODEBASE_OPTION = 
      OPTION_BUILDER
        .withShortName( "uri" )
        .withDescription( "Codebase URI (required)." )
        .withRequired( true )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "uri" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new URIValidator() )
            .create() )
        .create();
    
    private static final Option CODEBASE_OPTION = 
      OPTION_BUILDER
        .withShortName( "uri" )
        .withDescription( "Codebase URI." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "uri" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new URIValidator() )
            .create() )
        .create();
    
    private static final Option TITLE_OPTION = 
      OPTION_BUILDER
        .withShortName( "title" )
        .withDescription( "Title." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "name" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option USERNAME_OPTION = 
      OPTION_BUILDER
        .withShortName( "username" )
        .withDescription( "Username." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "username" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option PASSWORD_OPTION = 
      OPTION_BUILDER
        .withShortName( "password" )
        .withDescription( "Password." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "password" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option HOST_PRIORITY_OPTION = 
      OPTION_BUILDER
        .withShortName( "priority" )
        .withDescription( "Host priority." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "int" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( INTERGER_VALIDATOR )
            .create() )
        .create();

    private static final Option HOST_INDEX_OPTION = 
      OPTION_BUILDER
        .withShortName( "index" )
        .withDescription( "Host index path." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "resource" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option LAYOUT_OPTION = 
      OPTION_BUILDER
        .withShortName( "layout" )
        .withDescription( "Host layout strategy." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option HOST_SCHEME_OPTION = 
      OPTION_BUILDER
        .withShortName( "scheme" )
        .withDescription( "Host authentication scheme." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "scheme" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option HOST_PROMPT_OPTION = 
      OPTION_BUILDER
        .withShortName( "prompt" )
        .withDescription( "Host authentication prompt." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "prompt" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option TRUSTED_OPTION = 
      OPTION_BUILDER
        .withShortName( "trusted" )
        .withDescription( "Assert as trusted host." )
        .withRequired( false )
        .create();
        
    private static final Option UNTRUSTED_OPTION = 
      OPTION_BUILDER
        .withShortName( "untrusted" )
        .withDescription( "Assert as untrusted host." )
        .withRequired( false )
        .create();
    
    private static final Group TRUST_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( TRUSTED_OPTION )
        .withOption( UNTRUSTED_OPTION )
        .create();
    
    private static final Option ENABLED_OPTION = 
      OPTION_BUILDER
        .withShortName( "enabled" )
        .withDescription( "Enable the host." )
        .withRequired( false )
        .create();
        
    private static final Option DISABLED_OPTION = 
      OPTION_BUILDER
        .withShortName( "disabled" )
        .withDescription( "Disable the host." )
        .withRequired( false )
        .create();
        
    private static final Group ENABLED_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( ENABLED_OPTION )
        .withOption( DISABLED_OPTION )
        .create();
    
    private static final Option PROXY_EXCLUDE_OPTION = 
      OPTION_BUILDER
        .withShortName( "excludes" )
        .withDescription( "Proxy excludes." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "hosts" )
            .withMinimum( 1 )
            .withInitialSeparator( ' ' )
            .withSubsequentSeparator( ',' )
            .create() )
        .create();
        
    private static final Group SET_HANDLER_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( CODEBASE_OPTION )
        .withOption( TITLE_OPTION )
        .create();
    
    private static final Group ADD_HANDLER_OPTIONS_GROUP =
      GROUP_BUILDER
        .withOption( REQUIRED_CODEBASE_OPTION )
        .withOption( TITLE_OPTION )
        .create();
        
    private static final Group SET_HOST_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( HOST_OPTION )
        .withOption( USERNAME_OPTION )
        .withOption( PASSWORD_OPTION )
        .withOption( HOST_PRIORITY_OPTION )
        .withOption( HOST_INDEX_OPTION )
        .withOption( ENABLED_GROUP )
        .withOption( TRUST_GROUP )
        .withOption( LAYOUT_OPTION )
        .withOption( HOST_SCHEME_OPTION )
        .withOption( HOST_PROMPT_OPTION )
        .create();
    
    private static final Group ADD_HOST_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( REQUIRED_HOST_OPTION )
        .withOption( USERNAME_OPTION )
        .withOption( PASSWORD_OPTION )
        .withOption( HOST_PRIORITY_OPTION )
        .withOption( HOST_INDEX_OPTION )
        .withOption( DISABLED_OPTION )
        .withOption( TRUSTED_OPTION )
        .withOption( LAYOUT_OPTION )
        .withOption( HOST_SCHEME_OPTION )
        .withOption( HOST_PROMPT_OPTION )
        .create();
    
    private static final Group PROXY_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( HOST_OPTION )
        .withOption( USERNAME_OPTION )
        .withOption( PASSWORD_OPTION )
        .withOption( PROXY_EXCLUDE_OPTION )
        .create();
    
    private static final Group CACHE_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( DIRECTORY_OPTION )
        .withOption( LAYOUT_OPTION )
        .create();
    
    private static final Group SYSTEM_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( DIRECTORY_OPTION )
        .withOption( LAYOUT_OPTION )
        .create();
    
    private static final Option PROXY_COMMAND =
      COMMAND_BUILDER
        .withName( "proxy" )
        .withDescription( "Select proxy settings." )
        .withChildren( PROXY_OPTIONS_GROUP )
        .create();
    
    private static final Option SET_CACHE_COMMAND =
      COMMAND_BUILDER
        .withName( "cache" )
        .withDescription( "Select cache settings." )
        .withChildren( CACHE_OPTIONS_GROUP )
        .create();
    
    private static final Option SET_SYSTEM_COMMAND =
      COMMAND_BUILDER
        .withName( "system" )
        .withDescription( "Select system repository settings." )
        .withChildren( SYSTEM_OPTIONS_GROUP )
        .create();
    
    private static final Option ADD_HOST_COMMAND =
      COMMAND_BUILDER
        .withName( "host" )
        .withDescription( "Add a new resource host." )
        .withChildren( ADD_HOST_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option SET_HOST_COMMAND =
      COMMAND_BUILDER
        .withName( "host" )
        .withDescription( "Resource host selection." )
        .withChildren( SET_HOST_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option SET_LAYOUT_COMMAND =
      COMMAND_BUILDER
        .withName( "layout" )
        .withDescription( "Layout scheme selection." )
        .withChildren( SET_HANDLER_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option SELECT_HOST_COMMAND =
      COMMAND_BUILDER
        .withName( "host" )
        .withDescription( "Resource host selection." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option SELECT_LAYOUT_COMMAND =
      COMMAND_BUILDER
        .withName( "layout" )
        .withDescription( "Layout scheme selection." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option ADD_HANDLER_COMMAND =
      COMMAND_BUILDER
        .withName( "handler" )
        .withDescription( "Add a new content handler." )
        .withChildren( ADD_HANDLER_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "type" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option ADD_LAYOUT_COMMAND =
      COMMAND_BUILDER
        .withName( "layout" )
        .withDescription( "Add a new layout." )
        .withChildren( ADD_HANDLER_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "id" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option SET_HANDLER_COMMAND =
      COMMAND_BUILDER
        .withName( "handler" )
        .withDescription( "Content handler selection." )
        .withChildren( SET_HANDLER_OPTIONS_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "type" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option SELECT_HANDLER_COMMAND =
      COMMAND_BUILDER
        .withName( "handler" )
        .withDescription( "Content handler selection." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withName( "type" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
        
    private static final Option REMOVE_PROXY_COMMAND =
      COMMAND_BUILDER
        .withName( "proxy" )
        .withDescription( "Delete proxy settings." )
        .create();
        
    private static final Group ADD_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 1 )
        .withMinimum( 1 )
        .withOption( ADD_HOST_COMMAND )
        .withOption( ADD_HANDLER_COMMAND )
        .withOption( ADD_LAYOUT_COMMAND )
        .create();

    private static final Group SET_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 1 )
        .withMinimum( 1 )
        .withOption( PROXY_COMMAND )
        .withOption( SET_CACHE_COMMAND )
        .withOption( SET_SYSTEM_COMMAND )
        .withOption( SET_HOST_COMMAND )
        .withOption( SET_HANDLER_COMMAND )
        .withOption( SET_LAYOUT_COMMAND )
        .create();
    
    private static final Group REMOVE_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 1 )
        .withMinimum( 1 )
        .withOption( SELECT_HOST_COMMAND )
        .withOption( SELECT_HANDLER_COMMAND )
        .withOption( SELECT_LAYOUT_COMMAND )
        .withOption( REMOVE_PROXY_COMMAND )
        .create();
    
    private static final Option ADD_COMMAND =
      COMMAND_BUILDER
        .withName( "add" )
        .withChildren( ADD_OPTIONS_GROUP )
        .create();
    
    private static final Option SET_COMMAND =
      COMMAND_BUILDER
        .withName( "set" )
        .withDescription( "Set an configuration aspect." )
        .withChildren( SET_OPTIONS_GROUP )
        .create();
    
    private static final Option REMOVE_COMMAND =
      COMMAND_BUILDER
        .withName( "remove" )
        .withChildren( REMOVE_OPTIONS_GROUP )
        .create();
    
    private static final Option INFO_COMMAND =
      COMMAND_BUILDER
        .withName( "info" )
        .withDescription( "List configuration." )
        .create();

    private static final Option REVERT_COMMAND =
      COMMAND_BUILDER
        .withName( "revert" )
        .withDescription( "Set configuration to default." )
        .create();

    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withOption( INFO_COMMAND )
        .withOption( ADD_COMMAND )
        .withOption( SET_COMMAND )
        .withOption( REMOVE_COMMAND )
        .withOption( REVERT_COMMAND )
        .withOption( HELP_COMMAND )
        .withMinimum( 1 )
        .withMaximum( 1 )
        .create();
    
    private static final Group OPTIONS_GROUP =
      GROUP_BUILDER
        .withName( "command" )
        .withOption( PROFILE_URI_OPTION )
        .withOption( COMMAND_GROUP )
        .withMinimum( 0 )
        .create();

    private static CommandLine getCommandLine( Logger logger, String[] args )
    {
        try
        {
            // parse the command line arguments
        
            Parser parser = new Parser();
            parser.setGroup( OPTIONS_GROUP );
            return parser.parse( args );
        }
        catch( OptionException e )
        {
            logger.error( e.getMessage() );
            return null;
        }
    }
    
    private static void processHelp() throws IOException
    {
        processGeneralHelp( OPTIONS_GROUP );
    }
    
    private static void processGeneralHelp( Group group ) throws IOException
    {
        HelpFormatter formatter = new HelpFormatter( 
          HelpFormatter.DEFAULT_GUTTER_LEFT, 
          HelpFormatter.DEFAULT_GUTTER_CENTER, 
          HelpFormatter.DEFAULT_GUTTER_RIGHT, 
          100, 50 );
        
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getFullUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_GROUP_EXPANDED );
        
        formatter.setGroup( group );
        formatter.setShellCommand( "transit" );
        formatter.print();
    }
}

