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

package net.dpml.composition.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.part.Directive;
import net.dpml.part.EntryDescriptor;

import net.dpml.transit.model.UnknownKeyException;


/**
 * The ContextModel interface defines the remotely accessible component context. 
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ComponentDirective.java 2991 2005-07-07 00:00:04Z mcconnell@dpml.net $
 */
public interface ContextModel extends Remote
{
    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

   /**
    * Return the set of context entries descriptors.
    *
    * @return context entry descriptor array
    */
    EntryDescriptor[] getEntryDescriptors() throws RemoteException;
    
   /**
    * Return the current directive assigned to a context entry.
    * @param key the context entry key
    * @return the directive
    */
    Directive getEntryDirective( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Set a context entry directive value.
    * @param directive the context entry directive
    */
    void setEntryDirective( String key, Directive directive ) throws UnknownKeyException, RemoteException;

}
