/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.build;

import dpml.cli.Option;
import dpml.cli.Group;
import dpml.cli.CommandLine;
import dpml.cli.commandline.Parser;
import dpml.cli.util.HelpFormatter;
import dpml.cli.OptionException;
import dpml.cli.DisplaySetting;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.CommandBuilder;
import dpml.cli.option.PropertyOption;
import dpml.cli.validation.URIValidator;

import dpml.library.Module;
import dpml.library.Resource;
import dpml.library.Type;
import dpml.library.Classifier;
import dpml.library.Scope;

import dpml.library.impl.DefaultLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;

import javax.tools.Tool;
import javax.lang.model.SourceVersion;

import net.dpml.annotation.Component;
import net.dpml.lang.Strategy;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.SimpleServiceRegistry;
import net.dpml.util.Logger;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;


/**
 * Tool providing project info listing and supports delegation of project
 * building to a builder plugin.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( lifestyle=SINGLETON )
public class Main implements Tool
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    private static final URI BUILD_IMPLEMENTATION_URI;
    
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------
    
    private final Logger m_logger;
    private final Map m_map = new Hashtable();
    
    private boolean m_verbose;
    private boolean m_expand = false;
    private DefaultLibrary m_library;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Builder establishment. System property have already been assigned
    * to the current jvm by depot.  
    *
    * @param logger the assigned logging channel
    * @exception Exception if the build fails
    */
    public Main( Logger logger ) throws Exception
    {
        m_logger = logger;
    }
    
    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------
    
   /**
    * Return the source versions supported by this tool.
    * @return the set of source java versions
    */
    public Set<SourceVersion> getSourceVersions()
    {
        return Collections.unmodifiableSet(
          EnumSet.range(
            SourceVersion.RELEASE_0,
            SourceVersion.latest() ) );
    }
    
   /**
    * Run the build tool.
    * @param in the input stream
    * @param out the output stream
    * @param err the error stream
    * @param arguments the tool arguments
    * @return the logical result of executation - a value less than zero indicates failure
    */
    public int run( InputStream in, OutputStream out, OutputStream err, String... arguments )
    {

        // handle the simple case of $ depot -version

        try
        {
            CommandLine line = getCommandLine( arguments );
            if( line.hasOption( VERSION_OPTION ) )
            {
                printVersionMessage();
                return 0;
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error.";
            m_logger.error( error, e );
            return -1;
        }
 
        // setup the project/resource index

        try
        {
            m_library = new DefaultLibrary( m_logger );
        }
        catch( FileNotFoundException e )
        {
            final String error = 
              "Index not found.";
            m_logger.error( error );
            return -1;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load library index.";
            m_logger.error( error, e );
            return -1;
        }
        
        // execute the build
        
        try
        {
            return execute( arguments );
        }
        catch( Throwable e )
        {
            final String error = 
              "Build error.";
            m_logger.error( error, e );
            return -1;
        }
    }
    
    private int execute( String... args ) throws Exception
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "Initializing builder with " + args.length + " args." );
            for( String arg : args )
            {
                m_logger.trace( "\t" + arg );
            }
        }
        
        Thread.currentThread().setContextClassLoader( Builder.class.getClassLoader() );
        
        CommandLine line = getCommandLine( args );
        m_verbose = line.hasOption( VERBOSE_OPTION );

        if( line.hasOption( VERSION_OPTION ) )
        {
            printVersionMessage();
            return 0;
        }
        
        // setup the build version
        
        if( line.hasOption( SIGNATURE_OPTION ) )
        {
            String version = (String) line.getValue( SIGNATURE_OPTION, "SNAPSHOT" );
            System.setProperty( "build.signature", version );
            if( m_verbose )
            {
                m_logger.info( "Setting version to: " + version );
            }
        }
        
        if( line.hasOption( DECIMAL_OPTION ) )
        {
            System.setProperty( Resource.DECIMAL_VERSIONING_KEY, "true" );
            if( m_verbose )
            {
                m_logger.info( "Enabling decimal versioning." );
            }
        }
        
        try
        {
            if( line.hasOption( HELP_OPTION ) )
            {
                processHelp();
                return 0;
            }
            else
            {
                if( line.hasOption( LIST_OPTION ) )
                {
                    m_expand = line.hasOption( EXPAND_OPTION );
                    Resource[] resources = getTargetSelection( line );
                    if( resources.length == 0 )
                    {
                        m_logger.info( "Empty selection." );
                        return 0;
                    }
                    else
                    {
                        list( resources );
                        return 0;
                    }
                }
                else
                {
                    Resource[] resources = getSelection( line );
                    if( resources.length == 0 )
                    {
                        m_logger.error( "Empty selection." );
                        return -1;
                    }
                    return process( line, resources );
                }
            }
        }
        catch( OptionException e )
        {
            m_logger.error( e.getMessage() );
            return -1;
        }
    }

    private void printVersionMessage()
    {
        StringBuffer buffer = new StringBuffer( "version" );
        buffer.append( "\n" );
        buffer.append( "\n  DPML Depot" );
        buffer.append( "\n  Version @PROJECT-VERSION@" );
        buffer.append( "\n  Codebase " + getClass().getProtectionDomain().getCodeSource().getLocation() );
        buffer.append( "\n  Copyright 2005-2009 Stephen J. McConnell" );
        buffer.append( "\n  Digital Product Management Labs" );
        buffer.append( "\n" );
        String message = buffer.toString();
        m_logger.info( message );
    }
    
    private CommandLine getCommandLine( String[] args ) throws Exception
    {
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        return parser.parse( args );
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
        ArrayList<Resource> list = new ArrayList<Resource>();
        Resource[] targets = getTargetSelection( line );
        for( int i=0; i<targets.length; i++ )
        {
            Resource project = targets[i];
            if( Classifier.LOCAL.equals( project.getClassifier() ) )
            {
                list.add( project );
            }
        }
        return list.toArray( new Resource[ list.size() ] );
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
    * @return the exit value
    */
    private int process( CommandLine line, Resource[] resources )
    {
        boolean validation = line.hasOption( VALIDATE_OPTION );
        if( validation )
        {
            System.setProperty( "project.validation.enabled", "true" );
        }
        
        URI uri = (URI) line.getValue( BUILDER_URI_OPTION, BUILD_IMPLEMENTATION_URI );
        
            
        ServiceRegistry registry = 
          new SimpleServiceRegistry( 
            m_logger, 
            m_library, 
            new Boolean( m_verbose ) );
            
        Strategy strategy = loadBuilderStrategy( registry, uri );
        Builder builder = strategy.getInstance( Builder.class );
        
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
        
        List<?> list = line.getValues( TARGETS );
        String[] targets = (String[]) list.toArray( new String[ list.size() ] );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            try
            {
                boolean success = builder.build( resource, targets );
                if( !success )
                {
                    return -1;
                }
            }
            catch( Throwable e )
            {
                System.out.println( "# BUILD: " + e.toString() );
                final String error = 
                  "Unexpected error while building project ["
                  + resource.getResourcePath()
                  + "] under the basedir ["
                  + resource.getBaseDir()
                  + "].";
                getLogger().error( error, e );
                return -1;
            }
        }
        return 0;
    }
    
    private Strategy loadBuilderStrategy( final ServiceRegistry registry, final URI uri )
    {
        try
        {
            ClassLoader classloader = Builder.class.getClassLoader();
            return Strategy.load( classloader, registry, uri, null );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to load the builder plugin from the uri ["
              + uri 
              + "].";
            throw new BuildError( error, e );
        }
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
        if( null != resource.getVersion() )
        {
            print( pad + "version: " + resource.getVersion() );
        }
        print( pad + "basedir: " + resource.getBaseDir() );
        String p = pad + "  ";
        Type[] types = resource.getTypes();
        if( types.length > 0 )
        {
            print( pad + "types: (" + types.length + ")" );
            for( int i=0; i<types.length; i++ )
            {
                Type type = types[i];
                print( p + type.getID() );
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
    @SuppressWarnings( "unchecked" )
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
    
    private static final Option VERSION_OPTION = 
        OPTION_BUILDER
          .withShortName( "version" )
           .withDescription( "List version information and exit." )
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
    
    private static final Option SIGNATURE_OPTION = 
        OPTION_BUILDER
          .withShortName( "signature" )
          .withShortName( "sig" )
          .withDescription( "Build version signature." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Created artifact version signature." )
              .withName( "signature" )
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
          .withDescription( "Builder plugin uri." )
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
        .withOption( SIGNATURE_OPTION )
        .withOption( DECIMAL_OPTION )
        .withOption( VALIDATE_OPTION )
        .withOption( PROPERTY_OPTION )
        .create();
    
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withName( "options" )
        .withOption( HELP_OPTION )
        .withOption( VERSION_OPTION )
        .withOption( LIST_GROUP )
        .withOption( BUILD_GROUP )
        .withOption( TARGETS )
        .create();

    static
    {
        try
        {
            BUILD_IMPLEMENTATION_URI = new URI( "@DEPOT-BUILDER-URI@" );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "will not happen", e );
        }
    }

}

