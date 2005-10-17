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

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.tools.info.Scope;

/**
 * The Project interface describes information about a development project.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Project extends Remote
{
    Module getModule() throws RemoteException;

    String getProperty( String key ) throws RemoteException;
    
    String getProperty( String key, String value ) throws RemoteException;
    
    String getName() throws RemoteException;
    
    String getPath() throws RemoteException;

    String[] getTypes() throws RemoteException;
    
    File getBase() throws RemoteException;
        
    Resource[] getProviders( Scope scope ) 
      throws RemoteException, ResourceNotFoundException, ModuleNotFoundException;
    
    Resource[] getClassPath( Scope scope )
      throws RemoteException, ModuleNotFoundException, ResourceNotFoundException;
    
    Project[] getConsumers() 
      throws RemoteException, ResourceNotFoundException, ModuleNotFoundException;

   /**
    * Return the set projects that are consumers of this project.
    * @param depth the search depth
    * @return the sorted array of consumer projects
    */
    Project[] getAllConsumers() 
      throws RemoteException, ResourceNotFoundException, ModuleNotFoundException;

    Resource toResource() throws RemoteException;
}
