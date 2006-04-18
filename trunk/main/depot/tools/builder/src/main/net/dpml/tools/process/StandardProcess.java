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

import net.dpml.library.Resource;

import net.dpml.tools.model.Context;

import net.dpml.tools.tasks.PrepareTask;
import net.dpml.tools.tasks.InstallTask;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardProcess extends AbstractProcessor
{
    public void initialize( Context context )
    {
        context.init();
    }
    
    public void prepare( Context context )
    {
        context.getProject().log( "commencing preparation", Project.MSG_VERBOSE );
        Project project = context.getProject();
        final PrepareTask task = new PrepareTask();
        task.setProject( project );
        task.setTaskName( "prepare" );
        task.init();
        task.execute();
        context.getProject().log( "preparation complete", Project.MSG_VERBOSE );
    }
    
    public void install( Context context )
    {
        Project project = context.getProject();
        final InstallTask task = new InstallTask();
        task.setProject( project );
        task.setTaskName( "install" );
        task.init();
        task.execute();
    }
}
