/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.process;

import net.dpml.tools.tasks.PluginExportTask;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PluginProcess extends AbstractBuildListener
{
    /**
     * Signals that a target is starting.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetFinished( BuildEvent event )
    {
        Target target = event.getTarget();
        String name = target.getName();
        if( "build".equals( name ) )
        {
            Project project = event.getProject();
            final PluginExportTask task = new PluginExportTask();
            task.setProject( project );
            task.setTaskName( "export" );
            task.init();
            task.execute();
        }
    }
}
