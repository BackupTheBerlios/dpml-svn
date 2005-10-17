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

package net.dpml.tools.ant.tasks;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tools.ant.Target;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.ant.Phase;

/**
 * Alternative name for a project defintion.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ProcessTask extends GenericTask
{
    private Phase m_phase;
    
    public void setPhase( String phase )
    {
        m_phase = Phase.parse( phase );
    }
    
    public void execute()
    {
        Project project = getProject();
        Definition definition = getDefinition();
        Target[] targets = definition.getPluginTargets( m_phase, project );
        String[] names = new String[ targets.length ];
        Hashtable table = new Hashtable();
        for( int i=0; i<targets.length; i++ )
        {
            Target target = targets[i];
            String name = target.getName();
            table.put( name, target );
            names[i] = name;
        }
        Vector v = getProject().topoSort( names, table, true );
        for( int i=0; i<v.size(); i++ )
        {
            Target t = (Target) v.get( i );
            t.performTasks();
        }
    }
}
