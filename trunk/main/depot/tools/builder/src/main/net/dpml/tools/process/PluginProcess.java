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

import net.dpml.tools.model.Context;

import net.dpml.tools.tasks.PartTask;

import net.dpml.util.ExceptionHelper;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginProcess extends AbstractProcessor
{    
    public void pack( Context context )
    {
        try
        {
            PartTask task = new PartTask();
            Project project = context.getProject();
            task.setProject( project );
            task.setTaskName( "part" );
            task.init();
            task.execute();
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String message = 
              "Unexpected failure during plugin generation.";
            final String error =
              ExceptionHelper.packException( message, e, true );
            throw new BuildException( error, e );
        }
    }
}
