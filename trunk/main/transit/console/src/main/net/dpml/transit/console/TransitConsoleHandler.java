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
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;

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
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.validation.EnumValidator;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.URLValidator;
import net.dpml.cli.validation.NumberValidator;

import net.dpml.transit.TransitError;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.HostDirective;

/**
 * Plugin that handles station commands.
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
            m_logger = null;
            processHelp();
            System.exit( -1 );
        }
        else
        {
            m_logger = logger;
            m_line = line;
            m_directive = loadTransitDirective( line );
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
        else if( line.hasOption( SET_COMMAND ) )
        {
            processSet( line );
        }
        else
        {
            processHelp();
        }
    }
    
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
        
    private TransitDirective loadTransitDirective( CommandLine line )
    {
        if( line.hasOption( PROFILE_URI_OPTION ) )
        {
            URL url = (URL) line.getValue( PROFILE_URI_OPTION, null );
            try
            {
                InputStream input = url.openStream();
                XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
                return (TransitDirective) decoder.readObject();
            }
            catch( FileNotFoundException e )
            {
                final String error = 
                  "Resource not found: "
                + url;
                getLogger().warn( error );
                return null;
            }
            catch( Exception e )
            {
                final String error = 
                  "An error occured while attempting to load the transit profile: "
                  + url;
                getLogger().error( error, e  );
                return null;
            }
        }
        else
        {
            File prefs = Transit.DPML_PREFS;
            File config = new File( prefs, "transit.xml" );
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
    
    // ------------------------------------------------------------------------
    // command handling
    // ------------------------------------------------------------------------
    
    private void processInfo( CommandLine line )
    {
        StringBuffer buffer = new StringBuffer();
        ProxyDirective proxy = m_directive.getProxyDirective();
        if( null != proxy )
        {
            buffer.append( "\n\n  Proxy Settings" );
            buffer.append( "\n\n    Host: \t" + proxy.getHost() );
            if( proxy.getUsername() != null )
            {
                buffer.append( "\n    Username: \t" + proxy.getUsername() );
            }
            buffer.append( "\n    Password: \t" );
            char[] pswd = proxy.getPassword();
            if( null != pswd )
            {
                for( int i=0; i<pswd.length; i++ )
                {
                    buffer.append( "*" );
                }
            }
            buffer.append( "\n    Excludes: \t" + proxy.getExcludes() );
        }
        CacheDirective cache = m_directive.getCacheDirective();
        buffer.append( "\n\n  Cache Settings" );
        buffer.append( "\n\n    Directory: \t" + cache.getCache() );
        buffer.append( "\n    Layout: \t" + cache.getLayout() );
        buffer.append( "\n    Local: \t" + cache.getLocal() );
        
        HostDirective[] hosts = cache.getHostDirectives();
        buffer.append( "\n\n  Host Settings" );
        for( int i=0; i<hosts.length; i++ )
        {
            HostDirective host = hosts[i];
            buffer.append( "\n\n    " + host.getID() + " (" + (i+1) + ")" );
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
        
        System.out.println( buffer.toString() );
    }
    
    private void processSet( CommandLine line )
    {
        System.out.println( "Processing set request." );
    }
    
    // ------------------------------------------------------------------------
    // internal utilities
    // ------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    // ------------------------------------------------------------------------
    // static utilities
    // ------------------------------------------------------------------------
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
        
    private static final NumberValidator INTERGER_VALIDATOR = NumberValidator.getIntegerInstance();
    
    private static final Option PROFILE_URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "profile" )
          .withDescription( "Configuration profile." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "URL path." )
              .withName( "url" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URLValidator() )
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
            .withValidator( new URIValidator() )
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
        
    private static final Option HOST_LAYOUT_OPTION = 
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
        .withMinimum( 1 )
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
            .withName( "host" )
            .withMinimum( 1 )
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
        .withOption( HOST_LAYOUT_OPTION )
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
        .withOption( HOST_LAYOUT_OPTION )
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
    
    private static final Option PROXY_COMMAND =
      COMMAND_BUILDER
        .withName( "proxy" )
        .withDescription( "Select proxy settings." )
        .withChildren( PROXY_OPTIONS_GROUP )
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

    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withOption( INFO_COMMAND )
        .withOption( ADD_COMMAND )
        .withOption( SET_COMMAND )
        .withOption( REMOVE_COMMAND )
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

   /**
    * List general command help to the console.
    * @exception IOException if an I/O error occurs
    */
    private static void processHelp() throws IOException
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
        
        formatter.setGroup( OPTIONS_GROUP );
        formatter.setShellCommand( "transit" );
        formatter.print();
    }
    
}

