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

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.project.Context;
import org.apache.tools.ant.Task;


/**
 * Abstract task that hanldes the resolution of the current project context.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class ContextualTask extends Task
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private Context m_context;

    //------------------------------------------------------------------
    // Task
    //------------------------------------------------------------------

    public boolean isInitialized()
    {
        return ( m_context != null );
    }

    //------------------------------------------------------------------
    // ProjectTask
    //------------------------------------------------------------------

    public Context getContext()
    {
        if( null == m_context )
        {
            m_context = new Context( getProject() );
        }
        return m_context;
    }

    public AntFileIndex getIndex()
    {
        return getContext().getIndex();
    }

}
