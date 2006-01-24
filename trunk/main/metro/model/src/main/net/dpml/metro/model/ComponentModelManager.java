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

import net.dpml.metro.info.CollectionPolicy;

import net.dpml.part.ActivationPolicy;

/**
 * The MutableComponentModel interface extends ComponentModel with a set of 
 * operations supporting model changes.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentModelManager extends ComponentModelOperations
{
   /**
    * Set the component activation policy to the supplied value.
    * @param policy the new activation policy
    * @exception RemoteException if a remote exception occurs
    */
    void setActivationPolicy( ActivationPolicy policy ) throws RemoteException;

   /**
    * Override the assigned collection policy.
    * @param policy the collection policy value
    * @exception RemoteException if a remote exception occurs
    */
    void setCollectionPolicy( CollectionPolicy policy ) throws RemoteException;
}

