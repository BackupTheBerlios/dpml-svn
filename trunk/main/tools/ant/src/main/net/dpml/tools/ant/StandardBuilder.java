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

import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.model.Builder;

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
    private File m_template;
    private boolean m_verbose;

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new standard builder.
    *
    * @param logger assigned logging channel
    * @param model the transit model
    * @param template an ant template file
    * @param verbose verbose execution flag
    */
    public StandardBuilder( Logger logger, TransitModel model, File template, boolean verbose )
    {
        m_logger = logger;
        m_template = template;
        m_model = model;
        m_verbose = verbose;
        
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        String antHome = Environment.getEnvVariable( "ANT_HOME" );
        System.setProperty( "ant.home", antHome );
    }

    // ------------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------------

    public void build( net.dpml.tools.model.Project project ) throws Exception
    {
        Definition definition = new Definition( m_model, project );
        build( definition );
    }
    
    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------
    
    public void build( Definition definition ) throws Exception
    {
        Throwable error = null;
        
        Project project = createProject( definition, Phase.PREPARE );
        
        try
        {
            ProjectHelper helper = (ProjectHelper) project.getReference( "ant.projectHelper" );
            helper.parse( project, m_template );
            Vector targets = new Vector();
            
            // TODO: add targets from commandline
            if( targets.size() == 0 )
            {
                if( null != project.getDefaultTarget() )
                {
                    targets.addElement( project.getDefaultTarget() );
                }
            }
            
            if( targets.size() == 0 )
            {
                final String errorMessage =
                  "No targets requested and no default target declared.";
                throw new BuildException( errorMessage );
            }
            
            project.executeTargets( targets );
        }
        catch( Throwable e )
        {
            error = e;
        }
        finally
        {
            if( null != error )
            {
                if( error instanceof BuildException )
                {
                    if( m_logger.isDebugEnabled() )
                    {
                        Throwable cause = error.getCause();
                        m_logger.error( "Build failure.", cause );
                    }
                }
                else
                {
                    m_logger.error( "Build failure.", error );
                }
            }
            project.fireBuildFinished( error );
        }
    }

    public Project createProject( Definition definition, Phase phase ) throws Exception
    {
        Project project = new Project();
        project.setBaseDir( definition.getBase() );
        project.setDefaultInputStream( System.in );
        setupTransitComponentHelper( project );
        project.setCoreLoader( getClass().getClassLoader() );
        project.addBuildListener( createLogger() );
        Context context = new Context( definition, phase, project );
        project.addReference( "project.context", context );
        System.setIn( new DemuxInputStream( project ) );
        project.setProjectReference( new DefaultInputHandler() );
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference( "ant.projectHelper", helper );
        return project;
    }
    
    public void build( Definition definition, String type ) throws Exception
    {
        String templatePropertyName = "project.template." + type;
        String templateValue = definition.getProperty( templatePropertyName, null );
        if( null != templateValue )
        {
            URI uri = new URI( templateValue );
            URL url = uri.toURL();
            File file = (File) url.getContent( new Class[]{ File.class } );
            Project project = createProject( definition, Phase.PREPARE );
            ProjectHelper helper = (ProjectHelper) project.getReference( "ant.projectHelper" );
            helper.parse( project, file );
            Vector targets = new Vector();
            targets.add( project.getDefaultTarget() );
            
            String message = 
              "\nBuilding [" 
              + type 
              + "] using: " 
              + uri 
              + "#"
              + project.getDefaultTarget();

            project.log( message );
            project.executeTargets( targets );
        }
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

