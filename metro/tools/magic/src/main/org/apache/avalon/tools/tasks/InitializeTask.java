/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.tools.tasks;

import java.io.File;

import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.avalon.tools.model.Policy;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * The initialize task loads and plugins that a project
 * has declared under the &lt;plugins&gt; element. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class InitializeTask extends SystemTask
{
    public void execute() throws BuildException 
    {
        final Project project = getProject();

        //
        // if the project is running under gump then we need to 
        // check all of the project dependencies for gump.resource
        // overriding locations and if necessary - drag them into 
        // our local cache
        //

        if( getHome().isGump() )
        {
            setupGumpDependencies( project );
        }

        //
        // if the project declares plugin dependencies then install
        // these now
        //

        final String key = getContext().getKey();
        final Definition def = getHome().getDefinition( key );
        final ResourceRef[] refs = def.getPluginRefs();
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource plugin = getHome().getResource( ref );
            final String path = "plugin:" + plugin.getInfo().getSpec();
            final PluginTask task = new PluginTask();
            task.setTaskName( "plugin" );
            task.setProject( project );
            task.setUri( path );
            task.init();
            task.execute();
        }
    }

    private void setupGumpDependencies( Project project )
    {
        log( "Executing gump sanity check." );
        final String key = getContext().getKey();
        final Definition def = getHome().getDefinition( key );
        final ResourceRef[] refs =
          def.getResourceRefs( getProject(), Policy.ANY, ResourceRef.ANY, true );

        for( int i=0; i<refs.length; i++ )
        {
            Resource resource = getHome().getResource( refs[i] );
            if( !(resource instanceof Definition) )
            {
                String gumpKey = "gump.resource." + resource.getKey();
                String path = project.getProperty( gumpKey );
                if( null != path )
                {
                    updateCache( project, resource, path );
                }
                else
                {
                    final String warning = 
                      "Warning - missing property [" + gumpKey + "].";
                    project.log( warning );
                }
            }
        }

        final ResourceRef[] plugins = def.getPluginRefs();

        for( int i=0; i<plugins.length; i++ )
        {
            Resource resource = getHome().getResource( plugins[i] );
            if( !(resource instanceof Definition) )
            {
                String gumpKey = "gump.resource." + resource.getKey();
                String path = project.getProperty( gumpKey );
                if( null != path )
                {
                    updateCache( project, resource, path );
                }
                else
                {
                    final String warning = 
                      "Warning - missing property [" + gumpKey + "].";
                    project.log( warning );
                }
            }
        }
    }

    private void updateCache( Project project, Resource resource, String path )
    {
        getProject().log( 
          "Updating local cache for resource " + resource 
          + " using supplied path: " + path );

        File source = new File( path );
        if( !source.exists() )
        {
            final String error = 
              "Gump source resource override for resource " 
              + resource + " references a non-existant path [" + path 
              + "].";
            throw new BuildException( error );  
        }
        else
        {
            File local = resource.getArtifact( project, false );
            getProject().log( "Local cache file: [" + local + "].");

            local.getParentFile().mkdirs();
            Copy copy = (Copy) project.createTask( "copy" );
            copy.setTaskName( getTaskName() );
            copy.setFile( source );
            copy.setTofile( local );
            copy.setPreserveLastModified( true );
            copy.init();
            copy.execute();
        }
    }
}
