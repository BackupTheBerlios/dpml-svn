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
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ModelRuntimeException;
import net.dpml.tools.control.DefaultLibrary;


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
    private final TransitModel m_model;
    private final Class m_builderClass;
    private final Library m_library;
    private final boolean m_verbose;
    
    private String[] m_args;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * AntPlugin establishment.
    *
    * @param args supplimentary command line arguments
    * @exception Exception if the build fails
    */
    public BuildPlugin( TransitModel model, Logger logger, String[] args )
        throws Exception
    {
        m_logger = logger;
        m_library = new DefaultLibrary( logger );
        m_args = args;
        m_model = model;
        
        if( CLIHelper.isOptionPresent( args, "-v" ) )
        {
            m_args = CLIHelper.consolidate( args, "-v", 1 );
            m_verbose = true;
        }
        else
        {
            m_verbose = false;
        }
        
        ClassLoader classloader = Builder.class.getClassLoader();
        m_builderClass = Transit.getInstance().getRepository().getPluginClass( classloader, ANT_BUILDER_URI );
        
        String target = getTarget( args );
        if( isaProject( target ) )
        {
            Project project = m_library.getProject( target );
            boolean consumers = CLIHelper.isOptionPresent( args, "-consumers" );
            if( consumers )
            {
                listProjectConsumers( project );
            }
            else
            {
                processProject( project, args );
            }
        }
        else if( isaModule( target ) )
        {
            Module module = m_library.getModule( target );
            listModule( module );
        }
    }
    
    private void processProject( Project project, String[] args ) throws Exception
    {
        boolean build = CLIHelper.isOptionPresent( args, "-build" );
        if( build )
        {
            String template = project.getProperty( "project.template" );
            if( null != template )
            {
                File file = getTemplateFile( template );
                Object[] params = new Object[]{ m_logger, m_model, file, new Boolean( m_verbose ) };
                Builder builder = (Builder) Transit.getInstance().getRepository().instantiate( m_builderClass, params );
                builder.build( project );
            }
            else
            {
                final String error = 
                  "Project [" + project.getPath() + "] does not declare a template.";
                throw new Exception( error );
            }
        }
        else
        {
            listProject( project );
        }
    }
    
    private File getTemplateFile( String spec )
    {
        try
        {
            URI uri = new URI( spec );
            if( Artifact.isRecognized( uri ) )
            {
                URL url = uri.toURL();
                return (File) url.getContent( new Class[]{ File.class } );
            }
        }
        catch( Throwable e )
        {
        }
        return new File( spec );
    }
    
    private String getTarget( String[] args )
    {
        if( CLIHelper.isOptionPresent( args, "-project" ) )
        {
            String spec = CLIHelper.getOption( args, "-project" );
            m_args = CLIHelper.consolidate( args, "-project", 1 );
            return spec;
        }
        else if( CLIHelper.isOptionPresent( args, "-module" ) )
        {
            String spec = CLIHelper.getOption( args, "-module" );
            m_args = CLIHelper.consolidate( args, "-module", 1 );
            return spec;
        }
        else
        {
            for( int i=0; i<args.length; i++ )
            {
                String arg = args[i];
                if( !arg.startsWith( "-" ) )
                {
                    m_args = CLIHelper.consolidate( args, arg );
                    return arg;
                }
            }
        }
        m_args = args;
        return null;
    }
    
    private boolean isaModule( String spec ) throws Exception
    {
        try
        {
            m_library.getModule( spec );
            return true;
        }
        catch( ModuleNotFoundException pnfe )
        {
            return false;
        }
    }
    
    private boolean isaProject( String spec ) throws Exception
    {
        try
        {
            m_library.getProject( spec );
            return true;
        }
        catch( ProjectNotFoundException pnfe )
        {
            return false;
        }
    }
    
    private void listModule( Module module ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Listing module: " + module.getPath() );
        listModule( buffer, "  ", module );
        getLogger().info( buffer.toString() + "\n" );
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
    
    private void listProject( Project project ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Listing project: " + project.getPath() );
        listProject( buffer, "  ", project );
        getLogger().info( buffer.toString() + "\n" );
    }
    
    private void listProjectConsumers( Project project ) throws Exception
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
    
    private void listProject( StringBuffer buffer, String pad, Project project ) throws Exception
    {
        buffer.append( "\nproject: " + project.getPath() + "\n" );
        line( buffer, pad + "basedir: " + project.getBase() );
        String p = pad + "  ";
        Resource[] resources = project.getProviders( Scope.TEST );
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

