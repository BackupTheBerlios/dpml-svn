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

package net.dpml.tools.impl;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Vector;

import net.dpml.library.Builder;
import net.dpml.library.Library;
import net.dpml.library.Resource;

import net.dpml.tools.Context;
import net.dpml.tools.info.BuilderDirective;
import net.dpml.tools.info.BuilderDirectiveHelper;

import net.dpml.transit.Artifact;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.tools.MainTask;

import net.dpml.util.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.DemuxInputStream;

/**
 * The StandardBuilder is a plugin established by the Tools build controller
 * used for the building of a project based on the Ant build system in conjunction
 * with Transit plugin management services.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardBuilder implements Builder 
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * The default template uri path.
    */
    public static final String DEFAULT_TEMPLATE_URN = "local:template:dpml/tools/standard";
    
   /**
    * The builder configuration.
    */
    public static final BuilderDirective CONFIGURATION = loadConfiguration();
    
    private static BuilderDirective loadConfiguration()
    {
        try
        {
            return BuilderDirectiveHelper.build();
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to establish the builder configuration.";
            BuilderError be = new BuilderError( error, e );
            be.printStackTrace();
            return null;
        }
    }
    
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
    */
    public StandardBuilder( Logger logger, Library library )
    {
        this( logger, library, false );
    }
    
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
        
        Thread.currentThread().setContextClassLoader( Builder.class.getClassLoader() );
    }

    // ------------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------------

   /**
    * Build the project defined by the supplied resource.
    * @param resource the project definition
    * @param targets an array of build target names
    * @return the build success status
    */
    public boolean build( Resource resource, String[] targets )
    {
        Project project = createProject( resource );
        File template = getTemplateFile( resource );
        return build( resource, project, template, targets );
    }
    
   /**
    * Return the template for the resource.
    * @param resource the project definition
    * @return the template
    */
    public File getTemplateFile( Resource resource )
    {
        try
        {
            String systemOverride = System.getProperty( "project.template" );
            String override = resource.getProperty( "project.template", systemOverride );
            if( null != override )
            {
                File template = getTemplateFile( override );
                return template;
            }
            
            File basedir = resource.getBaseDir();
            String buildfile = resource.getProperty( "project.buildfile" );
            String defaultBuildfile = resource.getProperty( "project.standard.buildfile", "build.xml" );
            if( null != buildfile )
            {
                // there is an explicit 'project.buildfile' declaration in which case
                // we check for existance and fail if it does not exist
                
                File file = new File( basedir, buildfile );
                if( file.exists() )
                {
                    return file;
                }
                else
                {
                    final String error = 
                      "Resource buildfile ["
                      + file
                      + "] does not exist.";
                    throw new BuildException( error );
                }
            }
            else if( null != defaultBuildfile )
            {
                // check if a buildfile of the default name exists in the project's 
                // basedir - and if so - use it to build the project
                
                File file = new File( basedir, defaultBuildfile );
                if( file.exists() )
                {
                    return file;
                }
            }
            
            // otherwise we build using either an explicit or default template
            // resolved via a uri (typically a template stored in prefs)
            
            String defaultTemplateSpec = 
              resource.getProperty( "project.standard.template", DEFAULT_TEMPLATE_URN );
            String templateSpec = resource.getProperty( "project.template", defaultTemplateSpec );
                
            if( null != templateSpec )
            {
                File template = getTemplateFile( templateSpec );
                return template;
            }
            else
            {
                final String error = 
                  "Resource template property 'project.template' is undefined.";
                throw new BuildException( error );
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            m_result = e;
            final String error = 
              "Unexpected error while attempting to resolve project template."
              + "\nResource path: " 
              + resource.getResourcePath();
            throw new BuildException( error, e );
        }
    }
    
    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------
    
    boolean build( Resource resource, Project project, File template, String[] targets )
    {
        try
        {
            ProjectHelper helper = (ProjectHelper) project.getReference( "ant.projectHelper" );
            
            if( null != template )
            {
                helper.parse( project, template );
            }
            
            Vector vector = new Vector();
            
            if( targets.length == 0 )
            {
                if( null != project.getDefaultTarget() )
                {
                    vector.addElement( project.getDefaultTarget() );
                }
                else
                {
                    vector.addElement( CONFIGURATION.getDefaultPhase() );
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
                final String error = 
                  "Build failure."
                  + "\nProject: " + resource.getResourcePath()
                  + "\nBasedir: " + resource.getBaseDir()
                  + "\nTemplate: " + template 
                  + "\nLocation: " + e.getLocation();
                Throwable cause = e.getCause();
                m_logger.error( error, cause );
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
                return (File) url.getContent( new Class[]{File.class} );
            }
        }
        catch( Throwable e )
        {
        }
        return new File( spec );
    }
    
    Project createProject( Resource resource )
    {
        try
        {
            Project project = newProject();
            Context context = new DefaultContext( resource, project );
            return project;
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to establish build context." 
              + "\nProject: " + resource;
            throw new BuildException( error, e );
        }
    }
    
    private Project newProject() 
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
            MainTask task = new MainTask();
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
    
    private Logger getLogger()
    {
        return m_logger;
    }
}

