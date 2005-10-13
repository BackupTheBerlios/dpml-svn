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

package net.dpml.ant;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Vector;

import net.dpml.depot.Main;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;

import net.dpml.transit.tools.TransitComponentHelper;
import net.dpml.transit.tools.MainTask;

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

/**
 * Ant plugin running under Transit.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 * @version $Id: Metro.java 916 2004-11-25 12:15:17Z niclas@apache.org $
 */
public class AntPlugin 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The commandline args.
    */
    private String[] m_targets;

   /**
    * The target buildfile.
    */
    private File m_file = new File( "build.xml" );

   /**
    * Verbose mode.
    */
    private boolean m_verbose = false;

    private TransitModel m_model;
    private Logger m_logger;

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * AntPlugin establishment.
    *
    * @param args supplimentary command line arguments
    * @exception Exception if something goes pear-shaped
    */
    public AntPlugin( TransitModel model, Logger logger, String[] args )
        throws Exception
    {
        m_targets = args;
        m_model = model;
        m_logger = logger;

        if( Main.isOptionPresent( args, "-file" ) )
        {
            String path = Main.getOption( args, "-file" );
            m_file = new File( path );
            m_targets = Main.consolidate( m_targets, "-file", 1 );
        }

        if( Main.isOptionPresent( args, "-verbose" ) )
        {
            m_verbose = true;
            m_targets = Main.consolidate( m_targets, "-verbose" );
        }

        execute();
    }

    public void execute()
    {
        Throwable error = null;

        Project project = new Project();
        project.setDefaultInputStream( System.in );
        setupTransitComponentHelper( project );

        try
        {
            project.setCoreLoader( getClass().getClassLoader() );
            project.addBuildListener( createLogger() );

            System.setIn( new DemuxInputStream(project) );
            project.setProjectReference( new DefaultInputHandler() );
            project.fireBuildStarted();
            project.setUserProperty( "ant.file", m_file.getAbsolutePath() );
            project.init();

            ProjectHelper helper = ProjectHelper.getProjectHelper();
            ProjectHelper.configureProject( project, m_file );
            helper.parse( project, m_file );

            Vector targets = new Vector();
            for( int i=0; i < m_targets.length; i++ )
            {
                targets.add( m_targets[i] );
            }

            if( targets.size() == 0 ) 
            {
                if( null != project.getDefaultTarget() ) 
                {
                    targets.addElement( project.getDefaultTarget() );
                }
            }
            project.executeTargets(targets);
        }
        catch( Throwable e )
        {
            error = e;
        }
        finally
        {
            if( null != error )
            {
                m_logger.error( "Build error.", error );
                //System.out.println( "---------------------------------------" );
                //error.printStackTrace();
            }
            else
            {
                m_logger.info( "done" );
                //System.out.println( "done" );
            }
            project.fireBuildFinished( error );
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
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        return logger;
    }
}

