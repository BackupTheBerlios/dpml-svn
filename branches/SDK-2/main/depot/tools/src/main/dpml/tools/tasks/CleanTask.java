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

package dpml.tools.tasks;

import java.io.File;

import dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * Cleanup of generated target directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CleanTask extends GenericTask
{
   /**
    * Removes generated content through deletion of the declared
    * project target directory.
    */
    public void execute()
    {
        Context context = getContext();
        File dir = context.getTargetDirectory();
        Project project = context.getProject();
        final Delete task = new Delete();
        task.setProject( project );
        task.setTaskName( "delete" );
        task.setDir( dir );
        task.init();
        task.execute();
    }
}
