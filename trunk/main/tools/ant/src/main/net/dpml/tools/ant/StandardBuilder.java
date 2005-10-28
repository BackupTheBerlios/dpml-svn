/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.tools.ant;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.Vector;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.Environment;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.util.ExceptionHelper;
import net.dpml.transit.util.CLIHelper;
import net.dpml.transit.tools.TransitComponentHelper;
import net.dpml.transit.tools.MainTask;

import net.dpml.tools.model.Builder;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Resource;

import net.dpml.transit.Artifact;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.DemuxInputStream;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * The StandardBuilder is a plugin established by the Tools build controller
 * used for the building of a project based on the Ant build system in conjunction
 * with Transit plugin management services.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 * @version $Id: Metro.java 916 2004-11-25 12:15:17Z niclas@apache.org $
 */
public class StandardBuilder implements Builder 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Logger m_logger;
    private TransitModel m_model;
    private Library m_library;
    private boolean m_verbose;
    private Throwable m_result;

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new standard builder.
    *
    * @param logger assigned logging channel
    * @param library the library
    * @param verbose verbose execution flag
    */
    public StandardBuilder( Logger logger, Library library, boolean verbose )
    {
        m_logger = logger;
        m_verbose = verbose;
        m_library = library;
        
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        String antHome = Environment.getEnvVariable( "ANT_HOME" );
        System.setProperty( "ant.home", antHome );
    }

    // ------------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------------

    public boolean build( Resource resource, String[] targets )
    {
        try
        {
            Project project = createProject( resource );
            String templateSpec = resource.getProperty( "project.template" );
            if( null == templateSpec )
            {
                final String error = 
                  "Resource template property 'project.template' is undefined.";
                m_logger.error( error );
                return false;
            }
            File template = getTemplateFile( templateSpec );
            return build( project, template, targets );
        }
        catch( Throwable e )
        {
            m_result = e;
            final String error = 
              "Unexpected error while attempting to build project [" 
              + resource.getResourcePath()
              + "].";
            m_logger.error( error, e );
            return false;
        }
    }
    
    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------
    
    public boolean build( Project project, File template, String[] targets ) throws Exception
    {
        try
        {
            ProjectHelper helper = (ProjectHelper) project.getReference( "ant.projectHelper" );
            helper.parse( project, template );
            Vector vector = new Vector();
            
            if( targets.length == 0 )
            {
                if( null != project.getDefaultTarget() )
                {
                    vector.addElement( project.getDefaultTarget() );
                }
            }
            else
            {
                for( int i=0; i<targets.length; i++ )
                {
                    String target = targets[i];
                    vector.addElement( target );
                }
            }
            
            if( vector.size() == 0 )
            {
                final String errorMessage =
                  "No targets requested and no default target declared.";
                throw new BuildException( errorMessage );
            }
            
            project.executeTargets( vector );
            return true;
        }
        catch( BuildException e )
        {
            m_result = e;
            if( m_logger.isDebugEnabled() )
            {
                Throwable cause = e.getCause();
                m_logger.error( "Build failure.", cause );
            }
            return false;
        }
        finally
        {
            project.fireBuildFinished( m_result );
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
    
    public Project createProject( Resource resource ) throws Exception
    {
        Project project = createProject();
        project.setBaseDir( resource.getBaseDir() );
        Context context = new Context( resource, m_library, project );
        project.addReference( "project.context", context );
        return project;
    }
    
    public Project createProject() throws Exception
    {
        Project project = new Project();
        project.setSystemProperties();
        project.setDefaultInputStream( System.in );
        setupTransitComponentHelper( project );
        project.setCoreLoader( getClass().getClassLoader() );
        project.addBuildListener( createLogger() );
        System.setIn( new DemuxInputStream( project ) );
        project.setProjectReference( new DefaultInputHandler() );
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference( "ant.projectHelper", helper );
        return project;
    }
    
    private void setupTransitComponentHelper( Project project ) 
    {
        try
        {
            MainTask task = new MainTask( m_model, m_logger );
            task.setProject( project );
            task.init();
            task.execute();
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Setup failure.";
            throw new BuildException( error, e );
        }
    }
    
    private BuildLogger createLogger()
    {
        BuildLogger logger = new DefaultLogger();
        if( m_verbose )
        {
            logger.setMessageOutputLevel( Project.MSG_VERBOSE );
        }
        else
        {
            logger.setMessageOutputLevel( Project.MSG_INFO );
        }
        logger.setOutputPrintStream( System.out );
        logger.setErrorPrintStream( System.err );
        return logger;
    }
}

