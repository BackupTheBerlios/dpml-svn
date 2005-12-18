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

import java.rmi.RemoteException;

import net.dpml.metro.info.PartReference;
import net.dpml.part.Directive;

import net.dpml.transit.model.UnknownKeyException;


/**
 * The MutableContextModel interface extends ContextModel with operations supporting
 * context entry value mutation. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface MutableContextModel extends ContextModel
{
   /**
    * Set a context entry directive value.
    * @param key the context entry key
    * @param directive the context entry directive
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote exception occurs
    */
    void setEntryDirective( String key, Directive directive ) throws UnknownKeyException, RemoteException;

   /**
    * Apply an array of tagged directive as an atomic operation.  Application of 
    * directives to the context model is atomic such that changes all applied under a 
    * 'all-or-nothing' policy.
    *
    * @param directives an array of part references
    * @exception UnknownKeyException if a key within the array does not match a key within
    *   the context model.
    * @exception RemoteException if a remote exception occurs
    */
    void setEntryDirectives( PartReference[] directives ) throws UnknownKeyException, RemoteException;

}