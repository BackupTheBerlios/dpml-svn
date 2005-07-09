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

package net.dpml.magic.example.test;

import junit.framework.TestCase;
import net.dpml.magic.example.ExampleTask;
import org.apache.tools.ant.Project;


/**
 * IndexTestCase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ExampleTestCase extends TestCase
{
    public void testExampleTask() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        ExampleTask task = new ExampleTask();
        task.setProject( project );
        task.init();
        task.execute();
    }

}
