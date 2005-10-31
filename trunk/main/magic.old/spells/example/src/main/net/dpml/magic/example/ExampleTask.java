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

import net.dpml.magic.tasks.ProjectTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * A very simple spell.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ExampleTask extends ProjectTask
{
    private String m_message = "This is an example spell.\nURI: @MESSAGE@";

    public void setMessage( String message )
    {
        m_message = message;
    }

    private String getMessage()
    {
        return m_message;
    }

    public void execute() throws BuildException
    {
        final Project project = getProject();
        log( getMessage() );
    }
}
