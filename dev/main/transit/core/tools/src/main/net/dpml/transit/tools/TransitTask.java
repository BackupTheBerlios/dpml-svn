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
import java.net.URL;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitAlreadyInitializedException;
import net.dpml.transit.adapter.Adapter;
import net.dpml.transit.artifact.Handler;
import net.dpml.transit.monitors.Monitor;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.adapter.Adapter;
import net.dpml.transit.adapter.RepositoryMonitorAdapter;
import net.dpml.transit.adapter.CacheMonitorAdapter;
import net.dpml.transit.adapter.NetworkMonitorAdapter;
import net.dpml.transit.monitors.Monitor;

/**
 * Ant task that provides support for the import of build file templates
 * via an artifact url.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public abstract class TransitTask extends Task
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        System.setProperty( "dpml.transit.profile", "development" );
    }

    private static boolean m_INIT = false;

    public void setProject( Project project )
    {
        super.setProject( project );
        initialize( this );
    }

    public static void initialize( Task task ) throws BuildException
    {
        synchronized( TransitTask.class )
        {
            checkProperties( task.getProject() );
            if( true == m_INIT )
            {
                return;
            }
            try
            {
                Adapter logger = new AntAdapter( task );
                TransitModel model = new DefaultTransitModel( logger );
                Transit transit = Transit.getInstance( model );
                setupMonitors( transit, logger );
                m_INIT = true;
            }
            catch( TransitAlreadyInitializedException e )
            {
                // Transit is already initialized.
            }
            catch( Throwable e )
            {
                final String error =
                  "Internal error while initializing Transit";
                throw new BuildException( error, e );
            }
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
        File home = Transit.DPML_HOME;
        File system = Transit.DPML_SYSTEM;
        File data = Transit.DPML_DATA;
        File prefs = Transit.DPML_PREFS;

        File docs = new File( data, "docs" );
        File cache = new File( data, "cache" ); // <---  FIX ME !!
        File dist = new File( data, "dist" );
        File logs = new File( data, "logs" );

        updateProperty( project, "dpml.home", home.getAbsolutePath() );
        updateProperty( project, "dpml.system", system.getAbsolutePath() );
        updateProperty( project, "dpml.data", data.getAbsolutePath() );
        updateProperty( project, "dpml.docs", docs.getAbsolutePath() );
        updateProperty( project, "dpml.cache", cache.getAbsolutePath() );
        updateProperty( project, "dpml.dist", dist.getAbsolutePath() );
        updateProperty( project, "dpml.logs", dist.getAbsolutePath() );
        updateProperty( project, "dpml.prefs", prefs.getAbsolutePath() );

        String auth = project.getProperty( "dpml.transit.authority" );
        if( null != auth )
        {
            System.setProperty( "dpml.transit.authority", 
              System.getProperty( "dpml.transit.authority", auth ) );
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
    private static void setupMonitors( Transit instance, Adapter adapter ) throws Exception
    {
        instance.getRepositoryMonitorRouter().addMonitor(
          new RepositoryMonitorAdapter( adapter ) );
        instance.getCacheMonitorRouter().addMonitor(
          new CacheMonitorAdapter( adapter ) );
        instance.getNetworkMonitorRouter().addMonitor(
          new NetworkMonitorAdapter( adapter ) );
    }
}

