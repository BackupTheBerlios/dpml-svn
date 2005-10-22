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

package net.dpml.tools.control;

import java.io.File;
import java.net.URI;
import java.net.URL;

import net.dpml.transit.Logger;
import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.util.ExceptionHelper;
import net.dpml.transit.util.CLIHelper;

import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Builder;
import net.dpml.tools.model.Model;
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ModelRuntimeException;
import net.dpml.tools.control.DefaultLibrary;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

/**
 * Plugin that handles multi-project builds based on supplied commandline arguments.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 */
public class BuildPlugin 
{

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    private final DefaultLibrary m_library;
    private final boolean m_verbose;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * AntPlugin establishment.
    *
    * @param args supplimentary command line arguments
    * @exception Exception if the build fails
    */
    public BuildPlugin( Logger logger, String[] args )
        throws Exception
    {
        m_logger = logger;
        
        Options options = buildActionCommandLineOptions();
        //HelpFormatter formatter = new HelpFormatter();
        //formatter.printHelp( "build ", options, true );
        //CommandLineParser parser = new GnuParser();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse( options, args, false );
        m_verbose = ( line.hasOption( "verbose" ) || line.hasOption( "v" ) );
        if( line.hasOption( "version" ) )
        {
            String version = line.getOptionValue( "version" );
            System.setProperty( "build.signature", version );
        }
        else
        {
            System.setProperty( "build.signature", "SNAPSHOT" );
        }
        getLogger().debug( "building library" );
        m_library = new DefaultLibrary( logger );
        String selection = getSelectionArgument( line );
        if( null != selection )
        {
            getLogger().debug( "parsing selection: " + selection );
            Model[] models = m_library.select( selection );
            process( models, line );
        }
        else
        {
            String work = System.getProperty( "user.dir" );
            getLogger().debug( "resolving selection in: " + work );
            File file = new File( work ).getCanonicalFile();
            Model model = m_library.lookup( file );
            process( model, line );
        }
    }
    
    private String getSelectionArgument( CommandLine line )
    {
        if( line.hasOption( "select" ) )
        {
            return line.getOptionValue( "select" );
        }
        else if( line.hasOption( "s" ) )
        {
            return line.getOptionValue( "s" );
        }
        else
        {
            return null;
        }
    }
    
    private void process( Model[] models, CommandLine line ) throws Exception
    {
        if( models.length == 0 )
        {
            return;
        }
        else if( models.length == 1 )
        {
            Model model = models[0];
            process( model, line );
        }
        else
        {
            StringBuffer buffer = new StringBuffer( "Selection: " + models.length + "\n" );
            for( int i=0; i<models.length; i++ )
            {
                String span = "" + ( i+1 );
                if( span.length() < 5 )
                {
                    String lead = "     ".substring( 0, 5 - span.length() );
                    buffer.append( "\n" + lead + span + ": " + models[i].getPath() );
                }
                else
                {
                    buffer.append( "\n " + span + ":\t" + models[i].getPath() );
                }
            }
            getLogger().info( buffer.toString() );
        }
    }

    private void process( Model model, CommandLine line ) throws Exception
    {
        getLogger().debug( "processing: " + model );
        if( getListArgument( line ) || ( model instanceof Resource ) )
        {
            String[] remainder = line.getArgs();
            if( remainder.length > 0 )
            {
                StringBuffer buffer = new StringBuffer( "Ignoring (" + remainder.length + ") unrecognized arguments: " );
                for( int i=0; i<remainder.length; i++ )
                {
                    buffer.append( "[" + remainder[i] + "]" );
                    if( i < (remainder.length-1) )
                    {
                        buffer.append( ", " );
                    }
                }
                getLogger().warn( buffer.toString() );
            }
            if( model instanceof Module )
            {
                listModule( (Module) model, line );
            }
            else if( model instanceof Project )
            {
                listProject( (Project) model, line );
            }
            else
            {
                // TODO: resource listing
            }
        }
        else
        {
            String[] targets = line.getArgs();
            if( model instanceof Module )
            {
                System.out.println( "MODULE BUILD NOT IMPLEMENTED" );
            }
            else if( model instanceof Project )
            {
                boolean flag = line.hasOption( "consumers" );
                buildProject( (Project) model, line );
            }
        }
    }
    
    private boolean getListArgument( CommandLine line )
    {
        return ( line.hasOption( "list" ) || line.hasOption( "l" ) );
    }
    
    private boolean getConsumersArgument( CommandLine line )
    {
        return ( line.hasOption( "consumers" ) || line.hasOption( "c" ) );
    }
    
    private void buildProject( Project project, CommandLine line ) throws Exception
    {
        boolean flag = getConsumersArgument( line );
        String[] targets = line.getArgs();
        
        // TODO update the following to select the builder from a property declared on the project
        
        Object[] params = new Object[]{ m_logger, m_library, new Boolean( m_verbose ) };
        ClassLoader classloader = Builder.class.getClassLoader();
        Class builderClass = Transit.getInstance().getRepository().getPluginClass( classloader, ANT_BUILDER_URI );
        Builder builder = (Builder) Transit.getInstance().getRepository().instantiate( builderClass, params );
        if( flag )
        {
            Project[] consumers = project.getAllConsumers();
            StringBuffer buffer = new StringBuffer();
            buffer.append( 
              "building consumers of project [" 
              + project.getPath() 
              + "]\n" );
            for( int i=0; i<consumers.length; i++ )
            {
                int n = i+1;
                Project consumer = consumers[i];
                buffer.append( "\n" + n + "\t" + consumer.getPath() );
            }
            getLogger().info( buffer.toString() );
            for( int i=0; i<consumers.length; i++ )
            {
                Project consumer = consumers[i];
                boolean status = builder.build( consumer, targets );
                if( !status )
                {
                    break;
                }
            }
        }
        else
        {
            builder.build( project, targets );
        }
    }
    
    private void listModule( Module module, CommandLine line ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Listing module [" + module.getPath() + "]\n" );
        listModule( buffer, "", module );
        getLogger().info( buffer.toString() + "\n" );
        //Project[] projects = module.getSubsidiaryProjects();
        //for( int i=0; i<projects.length; i++ )
        //{
        //    Project project = projects[i];
        //    listProject( project, line );
        //}
    }
    
    private void listProject( Project project, CommandLine line ) throws Exception
    {
        boolean flag = line.hasOption( "consumers" );
        StringBuffer buffer = new StringBuffer();
        if( flag )
        {
            buffer.append( "Listing consumers of project: " + project.getPath() + "\n" );
            Project[] consumers = project.getAllConsumers();
            for( int i=0; i<consumers.length; i++ )
            {
                Project consumer = consumers[i];
                listProject( buffer, "  ", consumer );
                buffer.append( "\n" );
            }
        }
        else
        {
            buffer.append( "Listing project: " + project.getPath() + "\n" );
            listProject( buffer, "  ", project );
            buffer.append( "\n" );
        }
        getLogger().info( buffer.toString() );
    }
    
    private void listModule( StringBuffer buffer, String indent, Module module ) throws Exception
    {
        String pad = indent + "  ";
        String p = pad + "  ";
        buffer.append( "\n" + indent + "module: " +  module.getPath() + "\n" );
        line( buffer, pad + "version: " + module.getVersion() );
        //Module[] imports = module.getImportedModules();
        //if( imports.length > 0 )
        //{
        //    line( buffer, pad + "imports: (" + imports.length + ")" );
        //    for( int i=0; i<imports.length; i++ )
        //    {
        //        line( buffer, p + imports[i].getName() );
        //    }
        //}
        Module[] providers = module.getProviderModules( true );
        if( providers.length > 0 )
        {
            line( buffer, pad + "imports: (" + providers.length + ")" );
            for( int i=0; i<providers.length; i++ )
            {
                line( buffer, p + providers[i].getPath() );
            }
        }
        Module[] modules = module.getModules();
        if( modules.length > 0 )
        {
            line( buffer, pad + "modules: (" + modules.length + ")" );
            for( int i=0; i<modules.length; i++ )
            {
                line( buffer, p + modules[i].getName() );
            }
        }
        Resource[] resources = module.getResources();
        if( resources.length > 0 )
        {
            line( buffer, pad + "resources: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        Project[] projects = module.getProjects();
        if( projects.length > 0 )
        {
            line( buffer, pad + "projects: (" + projects.length + ")" );
            for( int i=0; i<projects.length; i++ )
            {
                line( buffer, p + projects[i].getName() );
            }
        }
    }
    
    private void listProject( StringBuffer buffer, String pad, Project project ) throws Exception
    {
        buffer.append( "\nproject: " + project.getPath() + "\n" );
        line( buffer, pad + "version: " + project.getVersion() );
        line( buffer, pad + "basedir: " + project.getBase() );
        String p = pad + "  ";
        Module[] imports = project.getProviderModules( true );
        if( imports.length > 0  )
        {
            line( buffer, pad + "imported modules: (" + imports.length + ")" );
            for( int i=0; i<imports.length; i++ )
            {
                Module module = imports[i];
                line( buffer, p + module.getPath() );
            }
        }
        Resource[] resources = project.getProviders();
        if( resources.length > 0 )
        {
            line( buffer, pad + "providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = project.getClassPath( Scope.BUILD );
        if( resources.length > 0 )
        {
            line( buffer, pad + "build classpath: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = project.getClassPath( Scope.RUNTIME );
        if( resources.length > 0 )
        {
            line( buffer, pad + "runtime classpath: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = project.getClassPath( Scope.TEST );
        if( resources.length > 0 )
        {
            line( buffer, pad + "test classpath: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        Project[] consumers = project.getConsumers();
        if( consumers.length > 0 )
        {
            line( buffer, pad + "consumers: (" + consumers.length + ")" );
            for( int i=0; i<consumers.length; i++ )
            {
                line( buffer, p + consumers[i].getName() );
            }
        }
    }
    
    private void listResource( StringBuffer buffer, String pad, Resource resource, String tag ) throws Exception
    {
        line( buffer, pad + tag + resource.getName() );
    }

    private void line( StringBuffer buffer, String message )
    {
        buffer.append( "\n" + message );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    /*
    private Options buildCommandLineOptions()
    {
        Options options = new Options();
        
        //
        // project or module selection
        //
        
        Option project = new Option( "project", true, "select the named project" );
        project.setArgName( "group/name" );
        
        Option module = new Option( "module", true, "select the named module" );
        module.setArgName( "group" );
        
        Option select = new Option( "select", true, "select the named module or project" );
        module.setArgName( "group[/name]" );
        
        OptionGroup selection = new OptionGroup();
        selection.addOption( select );
        selection.addOption( project );
        selection.addOption( module );
        options.addOptionGroup( selection );
        
        //
        // list switch
        //

        options.addOption( new Option( "list", false, "list the selection" ) );
        
        //
        // list switch
        //

        options.addOption( new Option( "v", false, "-verbose option" ) );
        options.addOption( new Option( "verbose", false, "enable verbose mode" ) );
        
        //
        // consumer switch
        //

        options.addOption( new Option( "consumers", false, "include all downstream consumer projects" ) );
        
        return options;
    }
    */
    
    private Options buildActionCommandLineOptions()
    {
        Options options = new Options();
        
        //
        // project or module selection
        //
        
        OptionGroup selection = new OptionGroup();
        Option s = new Option( "s", true, "-select" );
        Option select = new Option( "select", true, "select the named module or project" );
        select.setArgName( "group[/name]" );
        s.setArgName( "group[/name]" );
        selection.addOption( s );
        selection.addOption( select );
        options.addOptionGroup( selection );
        
        OptionGroup verbose = new OptionGroup();
        verbose.addOption( new Option( "v", false, "-verbose" ) );
        verbose.addOption( new Option( "verbose", false, "enable verbose mode" ) );
        options.addOptionGroup( verbose );
        
        //
        // list switch
        //

        OptionGroup listing = new OptionGroup();
        Option l = new Option( "l", false, "-list" );
        Option list = new Option( "list", false, "list the selection" );
        listing.addOption( l );
        listing.addOption( list );
        options.addOptionGroup( listing );
        
        //
        // consumer switch
        //

        OptionGroup consumption = new OptionGroup();
        Option c = new Option( "c", false, "-consumers" );
        Option consumers = new Option( "consumers", false, "select downstream consumer projects" );
        consumption.addOption( c );
        consumption.addOption( consumers );
        options.addOptionGroup( consumption );
        
        //
        // version modifier
        //

        Option version = new Option( "version", true, "build using an explicit version" );
        version.setArgName( "major.minor.micro" );
        options.addOption( version );
        
        //
        // test
        //

        Option test = new Option( "test", false, "internal testing" );
        options.addOption( test );
        
        return options;
    }
    
    private static final URI ANT_BUILDER_URI;
    
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

