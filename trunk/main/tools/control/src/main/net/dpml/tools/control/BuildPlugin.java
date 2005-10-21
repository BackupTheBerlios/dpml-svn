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
        m_library = new DefaultLibrary( logger );
        
        Options options = buildActionCommandLineOptions();
        //HelpFormatter formatter = new HelpFormatter();
        //formatter.printHelp( "build ", options, true );
        
        //CommandLineParser parser = new GnuParser();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse( options, args, false );
        String[] remainder = line.getArgs();
        
        if( !line.hasOption( "list" ) )
        {
            if( line.hasOption( "verbose" ) || line.hasOption( "v" ) )
            {
                m_verbose = true;
            }
            else
            {
                m_verbose = false;
            }
            String[] targets = line.getArgs();
            if( line.hasOption( "module" ) )
            {
                String spec = line.getOptionValue( "module" );
                Module module = m_library.getModule( spec );
                Project[] projects = module.getSubsidiaryProjects();
                for( int i=0; i<projects.length; i++ )
                {
                    Project project = projects[i];
                    buildProject( project, targets, false );
                }
            }
            else if( line.hasOption( "project" ) )
            {
                String spec = line.getOptionValue( "project" );
                Project project = m_library.getProject( spec );
                boolean flag = line.hasOption( "consumers" );
                buildProject( project, targets, flag );
            }
            else
            {
                String work = System.getProperty( "user.dir" );
                File file = new File( work ).getCanonicalFile();
                Model model = m_library.lookup( file );
                if( model instanceof Module )
                {
                    Module module = (Module) model;
                    Project[] projects = module.getSubsidiaryProjects();
                    for( int i=0; i<projects.length; i++ )
                    {
                        Project project = projects[i];
                        buildProject( project, targets, false );
                    }
                }
                else if( model instanceof Project )
                {
                    boolean flag = line.hasOption( "consumers" );
                    buildProject( (Project) model, targets, flag );
                }
            }
        }
        else
        {
            m_verbose = false;
            if( line.hasOption( "module" ) )
            {
                String spec = line.getOptionValue( "module" );
                Module module = m_library.getModule( spec );
                listModule( module );
            }
            else if( line.hasOption( "project" ) )
            {
                String spec = line.getOptionValue( "project" );
                Project project = m_library.getProject( spec );
                boolean flag = line.hasOption( "consumers" );
                listProject( project, flag );
            }
            else
            {
                String work = System.getProperty( "user.dir" );
                File file = new File( work ).getCanonicalFile();
                Model model = m_library.lookup( file );
                if( model instanceof Module )
                {
                    listModule( (Module) model );
                }
                else if( model instanceof Project )
                {
                    boolean flag = line.hasOption( "consumers" );
                    listProject( (Project) model, flag );
                }
            }
        }
        return;
    }
    
    private void buildProject( Project project, String[] targets, boolean flag ) throws Exception
    {
        // TODO update the following to select the builder from a property declared on the project
        
        Object[] params = new Object[]{ m_logger, m_library, new Boolean( m_verbose ) };
        ClassLoader classloader = Builder.class.getClassLoader();
        Class builderClass = Transit.getInstance().getRepository().getPluginClass( classloader, ANT_BUILDER_URI );
        Builder builder = (Builder) Transit.getInstance().getRepository().instantiate( builderClass, params );
        
        boolean ok = builder.build( project, targets );
        if( ok && flag )
        {
            Project[] consumers = project.getAllConsumers();
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
    }
    
    private void listModule( Module module ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Listing module: " + module.getPath() );
        listModule( buffer, "  ", module );
        getLogger().info( buffer.toString() + "\n" );
        
        Project[] projects = module.getSubsidiaryProjects();
        for( int i=0; i<projects.length; i++ )
        {
            Project project = projects[i];
            listProject( project, false );
        }
    }
    
    private void listProject( Project project, boolean flag ) throws Exception
    {
        if( flag )
        {
            StringBuffer buffer = new StringBuffer( "Listing consumers of project: " + project.getPath() + "\n" );
            Project[] consumers = project.getAllConsumers();
            for( int i=0; i<consumers.length; i++ )
            {
                Project consumer = consumers[i];
                listProject( buffer, "  ", consumer );
                buffer.append( "\n" );
            }
            getLogger().info( buffer.toString() );
        }
        else
        {
            StringBuffer buffer = new StringBuffer( "Listing project: " + project.getPath() );
            listProject( buffer, "  ", project );
            getLogger().info( buffer.toString() + "\n" );
        }
    }
    
    private void listModule( StringBuffer buffer, String pad, Module module ) throws Exception
    {
        String p = pad + "  ";
        Module[] imports = module.getImportedModules();
        if( imports.length > 0 )
        {
            line( buffer, pad + "imports: (" + imports.length + ")" );
            for( int i=0; i<imports.length; i++ )
            {
                line( buffer, p + imports[i].getName() );
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
        Module[] modules = module.getModules();
        if( modules.length > 0 )
        {
            line( buffer, pad + "modules: (" + modules.length + ")" );
            for( int i=0; i<modules.length; i++ )
            {
                line( buffer, p + modules[i].getName() );
            }
        }
    }
    
    private void listProject( StringBuffer buffer, String pad, Project project ) throws Exception
    {
        buffer.append( "\nproject: " + project.getPath() + "\n" );
        line( buffer, pad + "basedir: " + project.getBase() );
        String p = pad + "  ";
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
        Option list = new Option( "list", false, "list the selection" );
        Options options = new Options();
        options.addOption( list );

        //
        // project or module selection
        //
        
        Option project = new Option( "project", true, "select the named project" );
        project.setArgName( "group/name" );
        
        Option module = new Option( "module", true, "select the named module" );
        module.setArgName( "group" );
        
        Option select = new Option( "select", true, "select the named module or project" );
        select.setArgName( "group[/name]" );
        
        OptionGroup selection = new OptionGroup();
        selection.addOption( select );
        selection.addOption( project );
        selection.addOption( module );
        options.addOptionGroup( selection );
        
        options.addOption( new Option( "v", false, "-verbose option" ) );
        options.addOption( new Option( "verbose", false, "enable verbose mode" ) );
        
        //
        // consumer switch
        //

        options.addOption( new Option( "consumers", false, "include all downstream consumer projects" ) );
        
        return options;
    }
    
    private Options buildBuildCommandLineOptions()
    {
        Options options = new Options();
        options.addOption( new Option( "v", false, "-verbose option" ) );
        options.addOption( new Option( "verbose", false, "enable verbose mode" ) );
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

