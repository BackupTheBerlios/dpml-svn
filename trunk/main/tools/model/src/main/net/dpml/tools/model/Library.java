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

package net.dpml.tools.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.tools.info.ModuleDirective;

/**
 * The Library interface is the application root for module management.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Library extends Remote
{
   /**
    * Return a sorted array of all projects within the library.
    * @return the sorted project array
    */
    Project[] getAllProjects() 
      throws RemoteException, ModuleNotFoundException, ResourceNotFoundException;

   /**
    * Return a sorted array of projects including the dependent project of the 
    * suplied target project.
    * @param project the target project
    * @return the sorted project array
    */
    Project[] getAllProjects( Project project ) 
      throws RemoteException, ModuleNotFoundException, ResourceNotFoundException;

   /**
    * Get a named project.
    * @param path the project address include the module path
    * @exception ModuleNotFoundException if the address is not resolvable
    * @exception ProjectNotFoundException if the address is not resolvable
    */
    public Project getProject( String path ) 
      throws RemoteException, ModuleNotFoundException, ProjectNotFoundException;

   /**
    * Return an array of top-level modules registered with the library.
    * @return the module array
    */
    Module[] getModules() throws RemoteException;
    
   /**
    * Get a named module.
    * @param path the module address
    * @exception ModuleNotFoundException if the address is not resolvable
    */
    Module getModule( String path ) throws RemoteException, ModuleNotFoundException;
    
}
