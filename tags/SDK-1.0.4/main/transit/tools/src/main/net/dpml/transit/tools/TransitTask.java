/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.transit.tools;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitAlreadyInitializedException;
import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.monitor.Adapter;
import net.dpml.transit.monitor.RepositoryMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorAdapter;
import net.dpml.transit.monitor.NetworkMonitorAdapter;

/**
 * Ant task that provides support for the import of build file templates
 * via an artifact url.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class TransitTask extends Task
{
    private static boolean m_INIT = false;
    
    static
    {
        if( Transit.DPML_DATA != null )
        {
            // now we know that transit statics have been initialized
            boolean ok = true;
        }

        System.setProperty( "java.protocol.handler.pkgs", 
          System.getProperty( "java.protocol.handler.pkgs", "net.dpml.transit" ) );
    }

   /**
    * Set the project.
    * @param project the project
    */
    public void setProject( Project project )
    {
        super.setProject( project );
        initialize( this );
    }

   /**
    * Initialize the supplied task.
    * @param task the task to initialize
    * @exception BuildException if an initialization error occurs
    */
    public static void initialize( Task task ) throws BuildException
    {
        synchronized( TransitTask.class )
        {
            Project project = task.getProject();
            if( !m_INIT )
            {
                m_INIT = true;
                if( null == System.getProperty( "java.protocol.handler.pkgs" ) )
                {
                    System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
                }
                try
                {
                    Adapter logger = new AntAdapter( task );
                    TransitModel model = DefaultTransitModel.getDefaultModel( logger );
                    Transit transit = Transit.getInstance( model );
                    setupMonitors( transit, logger );
                }
                catch( TransitAlreadyInitializedException e )
                {
                    Adapter logger = new AntAdapter( task );
                    Transit transit = Transit.getInstance();
                    setupMonitors( transit, logger );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Internal error while initializing Transit";
                    throw new BuildException( error, e );
                }
            }
            checkProperties( project );
        }
    }

   /**
    * Declares four properties into the current project covering the DPML home directory,
    * template directory, doc and main cache directories.
    * @param project the current project
    * @exception BuildException if an execution error occurs
    */
    private static void checkProperties( Project project ) throws BuildException
    {
        if( null == project )
        {
            throw new NullPointerException( "project" );
        }

        String version = Transit.VERSION;
        updateProperty( project, "dpml.transit.version", version );

        File home = Transit.DPML_HOME;
        File system = Transit.DPML_SYSTEM;
        File data = Transit.DPML_DATA;
        File prefs = Transit.DPML_PREFS;

        File docs = new File( data, "docs" );
        File dist = new File( data, "dist" );
        File logs = new File( data, "logs" );

        updateProperty( project, "dpml.home", home.getAbsolutePath() );
        updateProperty( project, "dpml.system", system.getAbsolutePath() );
        updateProperty( project, "dpml.data", data.getAbsolutePath() );
        updateProperty( project, "dpml.docs", docs.getAbsolutePath() );
        updateProperty( project, "dpml.dist", dist.getAbsolutePath() );
        updateProperty( project, "dpml.logs", dist.getAbsolutePath() );
        updateProperty( project, "dpml.prefs", prefs.getAbsolutePath() );

        File cache = Transit.getInstance().getCacheDirectory();
        if( null == project.getReference( "dpml.cache" ) )
        {
            try
            {
                project.addReference( "dpml.cache", cache );
            }
            catch( Throwable e )
            {
                throw new BuildException( e );
            }
        }
        
        try
        {
            updateProperty( project, "dpml.cache", cache.getAbsolutePath() );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }

        String auth = project.getProperty( "dpml.transit.authority" );
        if( null != auth )
        {
            System.setProperty( 
              "dpml.transit.authority", 
              System.getProperty( 
                "dpml.transit.authority", auth ) 
            );
        }
    }

   /**
    * Update a property. If the property is already defined, the implementation
    * checks if the current value is the same as the supplied value and if not the
    * property value is updated.  If not property exists for the given key a new
    * property will be created.
    *
    * @param project the ant project
    * @param key the property key
    * @param value the property value
    */
    private static void updateProperty( Project project, String key, String value )
    {
        String v = project.getProperty( key );
        if( null == v )
        {
            project.setProperty( key, value );
        }
        else if( !v.equals( value ) )
        {
            project.setNewProperty( key, value );
        }
    }

   /**
    * Setup the monitors.
    */
    private static void setupMonitors( Transit instance, Adapter adapter )
    {
        try
        {
            instance.getRepositoryMonitorRouter().addMonitor(
              new RepositoryMonitorAdapter( adapter ) );
            instance.getCacheMonitorRouter().addMonitor(
              new CacheMonitorAdapter( adapter ) );
            instance.getNetworkMonitorRouter().addMonitor(
              new NetworkMonitorAdapter( adapter ) );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

