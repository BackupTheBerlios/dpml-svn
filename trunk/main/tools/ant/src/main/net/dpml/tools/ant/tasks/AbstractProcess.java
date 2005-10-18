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
import java.util.ArrayList;
import java.util.Vector;

import org.apache.tools.ant.Target;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.ant.Process;
import net.dpml.tools.ant.Phase;

/**
 * Alternative name for a project defintion.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public abstract class AbstractProcess extends GenericTask
{
    public void execute()
    {
        Project project = getProject();
        Definition definition = getDefinition();
        Phase phase = getPhase();
        Process[] processes = definition.getPluginTargets( project );
        for( int i=0; i<processes.length; i++ )
        {
            Process process = (Process) processes[i];
            if( process.supports( phase ) )
            {
                process.setProject( project );
                process.setOwningTarget( getOwningTarget() );
                process.setLocation( getLocation() );
                process.init();
                process.execute();
            }
        }
        
        /*
        
        String[] names = new String[ targets.length ];
        Hashtable table = new Hashtable();
        for( int i=0; i<targets.length; i++ )
        {
            Target target = targets[i];
            String name = target.getName();
            table.put( name, target );
            names[i] = name;
        }
        Phase phase = getPhase();
        Vector v = getProject().topoSort( names, table, true );
        for( int i=0; i<v.size(); i++ )
        {
            Process process = (Process) v.get( i );
            if( process.supports( phase ) )
            {
                process.performTasks();
            }
        }
        */
    }
}
