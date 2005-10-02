/*
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.part;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;

/**
 * A component context is a remotely manageble object that exposed context 
 * information used by a container in the establishment and runtime execution
 * of component instances.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public interface Context extends Remote
{    
    EntryDescriptor[] getEntries() throws RemoteException;

    Directive getDirective( String key ) throws UnknownKeyException, RemoteException;

    void setDirective( String key, Directive value ) throws UnknownKeyException, ContextException, RemoteException;

    String[] getChildKeys() throws RemoteException;

    Context getChild( String key ) throws UnknownKeyException, RemoteException;

}
