/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.component;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Model interfaces is used mark a object as manageable context used in 
 * the creation of a runtime handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Model extends Remote
{
   /**
    * The constant partition separator.
    */
    final String PARTITION_SEPARATOR = "/";
    
   /**
    * Return the path identifying the model.  A path commences with the
    * PARTITION_SEPARATOR character and is followed by a model name.  If the 
    * model exposed nested models, the path is composed of model names
    * seaprated by the PARTITION_SEPARATOR as in "/main/web/handler".
    *
    * @return the context path
    * @exception RemoteException if a remote exception occurs
    */
    String getContextPath() throws RemoteException;
 
   /**
    * Get the activation policy.  If the activation policy is STARTUP, an implementation
    * a handler shall immidiately activation a runtime instance.  If the policy is on DEMAND
    * an implementation shall defer activiation until an explicit request is received.  If 
    * the policy if SYSTEM activation may occur at the discretion of an implementation.
    *
    * @return the activation policy
    * @exception RemoteException if a remote exception occurs
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    ActivationPolicy getActivationPolicy() throws RemoteException;
    
   /**
    * Add a listener to the component model.
    * @param listener the model listener
    * @exception RemoteException if a remote exception occurs
    */
    void addModelListener( ModelListener listener ) throws RemoteException;
    
   /**
    * Remove a listener from the component model.
    * @param listener the model listener
    * @exception RemoteException if a remote exception occurs
    */
    void removeModelListener( ModelListener listener ) throws RemoteException;
}

