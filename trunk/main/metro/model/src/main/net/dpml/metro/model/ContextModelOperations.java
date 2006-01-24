/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.metro.info.EntryDescriptor;
import net.dpml.part.Directive;

import net.dpml.lang.UnknownKeyException;


/**
 * The ContextModelOperations interface defines the common local and remotely accessible component context. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ContextModelOperations
{
   /**
    * Return the set of context entries descriptors.
    *
    * @return context entry descriptor array
    * @exception RemoteException if a remote exception occurs
    */
    EntryDescriptor[] getEntryDescriptors() throws RemoteException;
    
   /**
    * Return  a of context entry descriptor.
    *
    * @param key the entry key
    * @return the entry descriptor
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote exception occurs
    */
    EntryDescriptor getEntryDescriptor( String key ) throws UnknownKeyException, RemoteException;
    
   /**
    * Return the current directive assigned to a context entry.
    * @param key the context entry key
    * @return the directive
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote exception occurs
    */
    Directive getEntryDirective( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Validate the model.
    * @exception ValidationException if one or more issues exist within the model
    * @exception RemoteException if a remote exception occurs
    */
    void validate() throws ValidationException, RemoteException;
}
