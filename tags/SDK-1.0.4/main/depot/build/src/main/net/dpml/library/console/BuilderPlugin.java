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

package net.dpml.library.console;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.util.Logger;

import net.dpml.lang.Part;

import net.dpml.library.Module;
import net.dpml.library.Resource;
import net.dpml.library.Builder;
import net.dpml.library.Type;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.Scope;
import net.dpml.library.impl.DefaultLibrary;

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
import net.dpml.cli.validation.URIValidator;

/**
 * Plugin that handles multi-project builds based on supplied commandline arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class BuilderPlugin
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    private static final URI ANT_BUILDER_URI;
    
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------
    
    private final Logger m_logger;
    private final DefaultLibrary m_library;
    private final Map m_map = new Hashtable();
    
    private boolean m_verbose;
    private boolean m_expand = false;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Builder establishment. System property have already been assigned
    * to the current jvm by depot.  
    *
    * @param logger the assigned logging channel
    * @param args supplimentary command line arguments
    * @exception Exception if the build fails
    */
    public BuilderPlugin( Logger logger, String[] args )
        throws Exception
    {
        m_logger = logger;
        
        Thread.currentThread().setContextClassLoader( Builder.class.getClassLoader() );
        
        CommandLine line = getCommandLine( args );
        m_verbose = line.hasOption( VERBOSE_OPTION );
        
        // setup the build version
        
        if( line.hasOption( VERSION_OPTION ) )
        {
            String version = (String) line.getValue( VERSION_OPTION, "SNAPSHOT" );
            System.setProperty( "build.signature", version );
            if( m_verbose )
            {
                getLogger().info( "Setting version to: " + version );
            }
        }
        
        if( line.hasOption( DECIMAL_OPTION ) )
        {
            System.setProperty( Resource.DECIMAL_VERSIONING_KEY, "true" );
            if( m_verbose )
            {
                getLogger().info( "Enabling decimal versioning." );
            }
        }
        
        m_library = new DefaultLibrary( logger );
        
        try
        {
            if( line.hasOption( HELP_OPTION ) )
            {
                processHelp();
                System.exit( 0 );
            }
            else
            {
                if( line.hasOption( LIST_OPTION ) )
                {
                    m_expand = line.hasOption( EXPAND_OPTION );
                    Resource[] resources = getTargetSelection( line );
                    if( resources.length == 0 )
                    {
                        getLogger().info( "Empty selection." );
                        System.exit( 0 );
                    }
                    else
                    {
                        list( resources );
                    }
                }
                else
                {
                    Resource[] resources = getSelection( line );
                    if( resources.length == 0 )
                    {
                        getLogger().info( "Empty selection." );
                        System.exit( 0 );
                    }
                    boolean result = process( line, resources );
                    if( !result )
                    {
                        System.exit( 1 );
                    }
                    else
                    {
                        System.exit( 0 );
                    }
                }
            }
        }
        catch( OptionException e )
        {
            m_logger.error( e.getMessage() );
        }
    }
    
    private CommandLine getCommandLine( String[] args ) throws Exception
    {
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        return parser.parse( args );
    }
        
    private Part createPart( URI uri ) throws Exception
    {
        try
        {
            Thread.currentThread().setContextClassLoader( Builder.class.getClassLoader() );
            return Part.load( uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error occured while attempting to load builder.\nURI: "
              + uri;
            throw new BuilderError( error, e );
        }
    }
    
    private Builder createBuilder( Part part ) throws Exception
    {
        Object[] params = new Object[]{m_logger, m_library, new Boolean( m_verbose )};
        return (Builder) part.instantiate( params );
    }
    
   /**
    * Resolve the project selection taking into account any overriding -s 
    * selection option, the -c switch, or in the absence of a selction, the 
    * implicit slection relative to the current working directory.
    *
    * @param line the commandline
    * @return the resolved array of resources sorted relative to build sequence
    */
    private Resource[] getSelection( CommandLine line ) throws Exception
    {
        ArrayList list = new ArrayList();
        Resource[] targets = getTargetSelection( line );
        for( int i=0; i<targets.length; i++ )
        {
            Resource project = targets[i];
            if( Classifier.LOCAL.equals( project.getClassifier() ) )
            {
                list.add( project );
            }
        }
        return (Resource[]) list.toArray( new Resource[ list.size() ] );
    }
    
   /**
    * Get the base selection and check if the consumer switch is present and 
    * if so build the consumers list from the selection list.
    * 
    * @param line the commandline
    * @return the array of projects in build order
    */
    private Resource[] getTargetSelection( CommandLine line ) throws Exception
    {
        Resource[] resources = getBaseSelection( line );
        if( resources.length == 0 )
        {
            return resources;
        }
        boolean flag = line.hasOption( CONSUMERS_OPTION );
        if( flag )
        {
            if( resources.length != 1 )
            {
                final String error = 
                  "Consumer resolution against a multi-element selection is not supported.";
                getLogger().error( error );
                return new Resource[0];
            }
            else
            {
                Resource resource = resources[0];
                return resource.getConsumers( true, true );
            }
        }
        else
        {
            return resources;
        }
    }
    
   /**
    * Get the set of projects taking into consideration either the 
    * overriding selection option or the base directory if no selection specificed.
    * 
    * @param line the commandline
    * @return the array of projects in build order
    */
    private Resource[] getBaseSelection( CommandLine line ) throws Exception
    {
        String selection = (String) line.getValue( SELECT_OPTION, null );
        if( null != selection )
        {
            getLogger().debug( "parsing selection: " + selection );
            Resource[] resources = m_library.select( selection, false, true );
            getLogger().debug( "selection: " + resources.length );
            return resources;
        }
        else
        {
            String work = System.getProperty( "user.dir" );
            getLogger().debug( "resolving selection in: " + work );
            File file = new File( work ).getCanonicalFile();
            Resource[] resources = m_library.select( file );
            return resources;
        }
    }

   /**
    * Build the supplied set of projects. If a build filure occurs then 
    * abort the build sequence and exit.
    *
    * @param line the commandline
    * @param resources the sorted sequence of projects to build
    */
    private boolean process( CommandLine line, Resource[] resources ) throws Exception
    {
        boolean validation = line.hasOption( VALIDATE_OPTION );
        if( validation )
        {
            System.setProperty( "project.validation.enabled", "true" );
        }
        URI uri = (URI) line.getValue( BUILDER_URI_OPTION, ANT_BUILDER_URI );
        if( resources.length > 1 )
        {
            StringBuffer buffer = 
              new StringBuffer( "Initiating build sequence: (" + resources.length + ")\n" );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                buffer.append( "\n  (" + ( i+1 ) + ")\t" + resource.getResourcePath() );
            }
            buffer.append( "\n" );
            getLogger().info( buffer.toString() );
        }
        
        List list = line.getValues( TARGETS );
        String[] targets = (String[]) list.toArray( new String[ list.size() ] );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            Part part = createPart( uri );
            Builder builder = createBuilder( part );
            boolean status = builder.build( resource, targets );
            if( !status )
            {
                return status;
            }
            System.gc();
        }
        return true;
    }
    
   /**
    * Build the supplied set of projects. If a build filure occurs then 
    * abort the build sequence and exit.
    *
    * @param resources the sorted sequence of prouject to build
    * @exception Exception if an error occurs
    */
    private void list( Resource[] resources ) throws Exception
    {
        if( resources.length == 1 )
        {
            Resource resource = resources[0];
            if( resource instanceof Module )
            {
                listModule( (Module) resource );
            }
            else
            {
                listResource( resource );
            }
        }
        else
        {
            listResources( resources );
        }
    }
    
    private void listModule( Module module ) throws Exception
    {
        print( "Listing module [" + module.getResourcePath() + "]\n"  );
        listResource( "  ", module, 0 );
        print( "" );
    }
    
    private void listResource( Resource project ) throws Exception
    {
        print( "Listing project: " + project.getResourcePath() + "\n"  );
        listResource( "  ", project, 0 );
        print( "" );
    }
    
    private void listResources( Resource[] resources ) throws Exception
    {
        print( "Selection: [" + resources.length + "]\n"  );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            String label = getLabel( i+1 );
            print( label + resource );
        }
        print( "" );
    }
    

    private void listResource( String pad, Resource resource, int n ) throws Exception
    {
        if( n > 0 )
        {
            print( "\n[" + n + "] " + resource );
        }
        else
        {
            print( "\n" + resource );
        }
        print( "" );
        print( pad + "version: " + resource.getVersion() );
        print( pad + "basedir: " + resource.getBaseDir() );
        String p = pad + "  ";
        Type[] types = resource.getTypes();
        if( types.length > 0 )
        {
            print( pad + "types: (" + types.length + ")" );
            for( int i=0; i<types.length; i++ )
            {
                print( p + types[i].getID() );
            }
        }

        Resource[] resources = resource.getProviders( Scope.BUILD, m_expand, true );
        if( resources.length > 0 )
        {
            print( pad + "build phase providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                Resource res = resources[i];
                print( p + res );
            }
        }
        resources = resource.getProviders( Scope.RUNTIME, m_expand, true );
        if( resources.length > 0 )
        {
            print( pad + "runtime providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                Resource res = resources[i];
                print( p + res );
            }
        }
        resources = resource.getProviders( Scope.TEST, m_expand, true );
        if( resources.length > 0 )
        {
            print( pad + "test providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                Resource res = resources[i];
                print( p + res );
            }
        }
    }
    
    static void print( String message )
    {
        System.out.println( message );
    }
    
    private static String getLabel( int n )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "  [" + n );
        buffer.append( "]" );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, 7 ) + " ";
    }
    
   /**
    * List general command help to the console.
    * @exception IOException if an I/O error occurs
    */
    private void processHelp() throws IOException
    {
        HelpFormatter formatter = new HelpFormatter( 
          HelpFormatter.DEFAULT_GUTTER_LEFT, 
          HelpFormatter.DEFAULT_GUTTER_CENTER, 
          HelpFormatter.DEFAULT_GUTTER_RIGHT, 
          100 );
        
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
        
        formatter.setGroup( COMMAND_GROUP );
        formatter.setShellCommand( "build" );
        formatter.print();
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();
    private static final PropertyOption PROPERTY_OPTION = new PropertyOption();
    
    private static final Option HELP_OPTION = 
        OPTION_BUILDER
          .withShortName( "help" )
          .withShortName( "h" )
          .withDescription( "List command help." )
          .withRequired( false )
          .create();
    
    private static final Option SELECT_OPTION = 
        OPTION_BUILDER
          .withShortName( "select" )
          .withShortName( "s" )
          .withDescription( "Build selected project(s)." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Project." )
              .withName( "pattern" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .create() )
          .create();
    
    private static final Option VERBOSE_OPTION = 
        OPTION_BUILDER
          .withShortName( "verbose" )
          .withShortName( "v" )
          .withDescription( "Enable verbose mode." )
          .withRequired( false )
          .create();
    
    private static final Option LIST_OPTION = 
        OPTION_BUILDER
          .withShortName( "list" )
          .withShortName( "l" )
          .withDescription( "List selected project(s)." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Project." )
              .withName( "pattern" )
              .withMinimum( 0 )
              .withMaximum( 1 )
              .create() )
          .create();
    
    private static final Option EXPAND_OPTION = 
        OPTION_BUILDER
          .withShortName( "expand" )
          .withShortName( "e" )
          .withDescription( "Expand dependencies." )
          .withRequired( false )
          .create();
    
    private static final Option CONSUMERS_OPTION = 
        OPTION_BUILDER
          .withShortName( "consumers" )
          .withShortName( "c" )
          .withDescription( "Consumer switch." )
          .withRequired( false )
          .create();
    
    private static final Option VALIDATE_OPTION = 
        OPTION_BUILDER
          .withShortName( "validate" )
          .withDescription( "Enable deliverable validation." )
          .withRequired( false )
          .create();
    
    private static final Option VERSION_OPTION = 
        OPTION_BUILDER
          .withShortName( "version" )
          .withDescription( "Build version." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Created artifact version." )
              .withName( "version" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .create() )
          .create();

    private static final Option DECIMAL_OPTION = 
        OPTION_BUILDER
          .withShortName( "decimal" )
          .withDescription( "Enable decimal versioning." )
          .withRequired( false )
          .create();
    
    private static final Option BUILDER_URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "plugin" )
          .withDescription( "Default builder plugin uri." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Artifact reference." )
              .withName( "artifact" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
    
    private static final Option TARGETS = 
        ARGUMENT_BUILDER
          .withName( "target" )
          .create();

    private static final Group LIST_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( LIST_OPTION )
        .withOption( EXPAND_OPTION )
        .withOption( CONSUMERS_OPTION )
        .create();
    
    private static final Group BUILD_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( SELECT_OPTION )
        .withOption( CONSUMERS_OPTION )
        .withOption( VERBOSE_OPTION )
        .withOption( BUILDER_URI_OPTION )
        .withOption( VERSION_OPTION )
        .withOption( DECIMAL_OPTION )
        .withOption( VALIDATE_OPTION )
        .withOption( PROPERTY_OPTION )
        .create();
    
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withName( "options" )
        .withOption( HELP_OPTION )
        .withOption( LIST_GROUP )
        .withOption( BUILD_GROUP )
        .withOption( TARGETS )
        .create();

    static
    {
        try
        {
            ANT_BUILDER_URI = new URI( "@ANT-BUILDER-URI@" );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "will not happen", e );
        }
    }

}

