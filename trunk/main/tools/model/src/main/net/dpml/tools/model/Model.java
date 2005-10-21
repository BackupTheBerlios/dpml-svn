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

/**
 * The Modele interface defines a node within a module hierachy.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Model extends Remote
{
   /**
    * Return the name of the model.
    */
    String getName() throws RemoteException;
    
   /**
    * Return the fully qualified name of the model.
    */
    String getPath() throws RemoteException;
    
   /**
    * Return the enclosing module.
    */
    Module getModule() throws RemoteException;

   /**
    * Return the version.
    */
    String getVersion() throws RemoteException;
}
