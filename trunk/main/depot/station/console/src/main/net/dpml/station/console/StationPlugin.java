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

package net.dpml.station.console;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;

import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.util.HelpFormatter;
import net.dpml.cli.OptionException;
import net.dpml.cli.DisplaySetting;


/**
 * Plugin that handles station commands.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StationPlugin 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new station plugin.
    *
    * @param logger the assigned logging channel
    * @param args supplimentary command line arguments
    */
    public StationPlugin( Logger logger, String[] args ) throws IOException
    {
        m_logger = logger;
        
        for( int i=0; i<args.length; i++ )
        {
            System.out.println( "" + (i+1) + " " + args[i] );
        }
        
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();
        CommandBuilder cBuilder = new CommandBuilder();
        GroupBuilder gBuilder = new GroupBuilder();

        final String startupMessage = 
          "Startup the Station as a background process. The station "
          + "will locate (and establish if necessary) an rmi registry "
          + "using the supplied url or port (defaulting to localhost:1099). "
          + "During startup any registered applications with a AUTOMATIC "
          + "startup policy will be deployed.";
        
        final String addMessage = 
          "Add a new application to the station.  The add command application key "
          + "argument estlishes a new application key.  If the key is already assigned "
          + "an error will be raised.  The add command requires the -urn <codebase> option "
          + "identifying the application codebase that this application will use during "
          + "deployment.  The codebase uri may be a plugin or a part uri. If the add command "
          + "includes the -policy option the policy will be applied following successfull "
          + "addition of application profile.";
          
        Option policy = 
          oBuilder
            .withShortName( "policy" )
            .withDescription( "application startup policy" )
            .withRequired( false )
            .withArgument(
              aBuilder 
                .withDescription( "disabled|manual|automatic" )
                .withName( "policy" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        
        Option uri = 
          oBuilder
            .withShortName( "uri" )
            .withDescription( "application codebase uri" )
            .withRequired( false )
            .withArgument(
              aBuilder 
                .withDescription( "codebase uri" )
                .withName( "artifact" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        
        Option basedir = 
          oBuilder
            .withShortName( "dir" )
            .withShortName( "basedir" )
            .withDescription( "application base directory" )
            .withRequired( false )
            .withArgument(
              aBuilder 
                .withDescription( "directory path" )
                .withName( "path" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
            
        Option properties = new PropertyOption();
        
        Group addOptionsGroup =
          gBuilder
            .withOption(
              oBuilder
                .withShortName( "uri" )
                .withDescription( "application codebase uri" )
                .withRequired( true )
                .withArgument(
                  aBuilder
                    .withDescription( "plugin or part uri" )
                    .withName( "artifact" )
                    .withMinimum( 1 )
                    .withMaximum( 1 )
                    .create() )
                  .create()
            )
            .withMinimum( 1 )
            .withOption( policy )
            .withOption( properties )
            .withOption( basedir )
            .create();

        Group setOptionsGroup =
          gBuilder
            .withOption( uri )
            .withMinimum( 1 )
            .withOption( policy )
            .withOption( properties )
            .withOption( basedir )
            .create();
            
        Option startup = 
          cBuilder
            .withName( "startup" )
            .withDescription( startupMessage )
            .withArgument(
              aBuilder
                .withDescription( "rmi registry host address" )
                .withName( "host" )
                .withMinimum( 0 )
                .withMaximum( 1 )
                .create() )
            .create();
        Option shutdown = 
          cBuilder
            .withName( "shutdown" )
            .withDescription( "shutdown the station process" )
            .create();
        Option help = 
          cBuilder
            .withName( "help" )
            .withDescription( "print command help" )
            .create();
        Option add = 
          cBuilder
            .withName( "add" )
            .withDescription( addMessage )
            .withArgument(
              aBuilder
                .withDescription( "unique application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .withChildren( addOptionsGroup )
            .create();
        Option set = 
          cBuilder
            .withName( "set" )
            .withDescription( "set an application feature" )
            .withArgument(
              aBuilder
                .withDescription( "application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .withChildren( setOptionsGroup )
            .create();
        Option start = 
          cBuilder
            .withName( "start" )
            .withDescription( "start the application" )
            .withArgument(
              aBuilder
                .withDescription( "application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        Option stop = 
          cBuilder
            .withName( "stop" )
            .withDescription( "stop the application" )
            .withArgument(
              aBuilder
                .withDescription( "application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        Option restart = 
          cBuilder
            .withName( "restart" )
            .withDescription( "restart the application" )
            .withArgument(
              aBuilder
                .withDescription( "application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        Option remove = 
          cBuilder
            .withName( "remove" )
            .withDescription( "remove an application from the station" )
            .withArgument(
              aBuilder
                .withDescription( "unique application key" )
                .withName( "key" )
                .withMinimum( 1 )
                .withMaximum( 1 )
                .create() )
            .create();
        Option list = 
          cBuilder
            .withName( "list" )
            .withDescription( 
              "if no application key is supplied then list all installed application keys "
              + "otherwise list information about the named application" )
            .withArgument(
              aBuilder
                .withDescription( "application name" )
                .withName( "name" )
                .withMinimum( 0 )
                .withMaximum( 1 )
                .create() )
            .create();
        
        Group group = 
          gBuilder
            .withName( "options" )
            .withOption( startup )
            .withOption( add )
            .withOption( set )
            .withOption( start )
            .withOption( stop )
            .withOption( restart )
            .withOption( list )
            .withOption( remove )
            .withOption( shutdown )
            .withOption( help )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create();
        
        Parser parser = new Parser();
        parser.setGroup( group );
        
        try
        {
            CommandLine line = parser.parse( args );
            List options = line.getOptions();
            Iterator optionsIterator = options.iterator();
            while( optionsIterator.hasNext() )
            {
                 Option option = (Option) optionsIterator.next();
                System.out.println( "# OPTION: " + option + " [" + option.getId() + "]" );
            }
            if( line.hasOption( help ) )
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_GROUP_ARGUMENT );
                formatter.getFullUsageSettings().remove( DisplaySetting.DISPLAY_GROUP_EXPANDED );
                formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
                formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
                formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_GROUP_ARGUMENT );
                formatter.setGroup( group );
                formatter.setShellCommand( "station" );
                formatter.print();
            }
            else if( line.hasOption( startup ) )
            {
                Object value = line.getValue( startup, null );
                if( null == value )
                {
                    System.out.println( "# startup on default host and port" );
                }
                else
                {
                    System.out.println( "# startup on [" + value + "]" );
                }
            }
            else if( line.hasOption( shutdown ) )
            {
                System.out.println( "# shutdown" );
            }
            else if( line.hasOption( add ) )
            {
                Object value = line.getValue( add, null );
                if( null == value )
                {
                    throw new NullPointerException( "key" );
                }
                System.out.println( "# add" );
                Object uriValue = line.getValue( uri, null );
                System.out.println( "# key [" + value + "]" );
                System.out.println( "# uri [" + uriValue + "]" );
                System.out.println( "# props [" + line.getProperties().size() + "]" );
                Set propertyValue = line.getProperties();
                Iterator iterator = propertyValue.iterator();
                while( iterator.hasNext() )
                {
                    String name = (String) iterator.next();
                    String v = line.getProperty( name );
                    System.out.println( "# PROP " + name + ", " + v );
                }
                String baseDirValue = (String) line.getValue( basedir, null );
                System.out.println( "# basedir [" + baseDirValue + "]" );
            }
            else if( line.hasOption( list ) )
            {
                Object value = line.getValue( list, null );
                if( null == value )
                {
                    System.out.println( "# list all applications" );
                }
                else
                {
                    System.out.println( "# list [" + value + "]" );
                }
            }
            else
            {
                Iterator iterator = options.iterator();
                while( iterator.hasNext() )
                {
                    Option option = (Option) iterator.next();
                    System.out.println( "# UNRECOGNIZED OPTION: " + option + " [" + option.getId() + "]" );
                }
            }
        }
        catch( OptionException e )
        {
            m_logger.error( e.getMessage() );
        }
    }
}

