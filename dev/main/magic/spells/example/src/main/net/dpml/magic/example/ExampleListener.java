/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.example;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * A very simple spell.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ExampleListener implements BuildListener
{
    private static final String PREPARE_TASK_KEY =
      "plugin:dpml/magic/dpml-magic-core:prepare";

    private Project m_project;

    public ExampleListener( Project project )
    {
        m_project = project;
    }

    /**
     * Listen for the completion of the prepare task.
     *
     * @param event the build event
     */
    public void taskFinished(BuildEvent event)
    {
        String type = event.getTask().getTaskType();
        if( PREPARE_TASK_KEY.equals( type ) )
        {
            ExampleTask task = new ExampleTask();
            task.setProject( m_project );
            task.setTaskName( "example" );
            task.init();
            task.execute();
        }
    }

    public void buildStarted(BuildEvent event)
    {
    }

    public void buildFinished(BuildEvent event)
    {
    }

    public void targetStarted(BuildEvent event)
    {
    }

    public void targetFinished(BuildEvent event)
    {
    }

    public void taskStarted(BuildEvent event)
    {
    }

    public void messageLogged(BuildEvent event)
    {
    }

}
