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

package net.dpml.magic.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import net.dpml.magic.AntFileIndex;

/**
 * Announce the initiation of a build.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class InfoTask extends ContextualTask
{
    private boolean m_ReportMemory = false;

    public void init()
    {
        super.init();
        String value = getProject().getProperty( "magic.memory.report" );
        if( null != value )
        {
            m_ReportMemory = value.equalsIgnoreCase( "true" );
        }
    }

    public void execute() throws BuildException
    {
        final Project project = getProject();
        project.log( AntFileIndex.BANNER );
        String key = getContext().getKey();
        project.log( "key: " + key );

        if( m_ReportMemory )
        {
            Runtime rt = Runtime.getRuntime();
            rt.gc();
            project.log( "    memory total: " + rt.totalMemory() );
            project.log( "    memory   max: " + rt.maxMemory() );
            project.log( "    memory  free: " + rt.freeMemory() );
        }
        project.log( AntFileIndex.BANNER );
    }
}
