/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */

/*
 * One has to add the builder to a project first.
 * 
   IProjectDescription desc = project.getDescription();
   ICommand[] commands = desc.getBuildSpec();
   boolean found = false;

   for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(BUILDER_ID)) {
         found = true;
         break;
      }
   }
   if (!found) { 
      //add builder to project
      ICommand command = desc.newCommand();
      command.setBuilderName(BUILDER_ID);
      ICommand[] newCommands = new ICommand[commands.length + 1];

      // Add it before other builders.
      System.arraycopy(commands, 0, newCommands, 1, commands.length);
      newCommands[0] = command;
      desc.setBuildSpec(newCommands);
      project.setDescription(desc, null);
   }


 */
package org.apache.metro.studio.eclipse.ui.tools;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ExampleBuilder extends IncrementalProjectBuilder
{

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IProject[] build( int pKind, Map pArgs, IProgressMonitor pMonitor ) 
        throws CoreException
    {
        System.out.println( "here" );
        // TODO Auto-generated method stub
        return null;
    }

    public ExampleBuilder()
    {
        super();
        // TODO Auto-generated constructor stub
    }
}
