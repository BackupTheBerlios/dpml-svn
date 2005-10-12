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

/**
 * The Modele interface defines a node within a module hierachy.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Module extends Remote
{
    String getName() throws RemoteException;
    
    String getPath() throws RemoteException;
    
    Module getParent() throws RemoteException;
    
    Module[] getModules() throws RemoteException;
    
    Module getModule( String key ) throws RemoteException, ModuleNotFoundException;
    
    Resource[] getResources() throws RemoteException;
    
    Resource getResource( String key ) throws RemoteException, ResourceNotFoundException;
    
    Project[] getProjects() throws RemoteException;
    
    Project getProject( String key ) throws RemoteException, ProjectNotFoundException;
    
    Resource resolveResource( String key ) throws RemoteException, ResourceNotFoundException;
}
