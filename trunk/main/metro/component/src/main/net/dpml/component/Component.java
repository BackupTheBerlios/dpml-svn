/*
 * Copyright (c) 2005-2006 Stephen J. McConnell
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Component represents a remote interface to a runtime component type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Component extends Remote, Commissionable
{
   /**
    * Return a provider of an instance of the component.
    * @return the instance provider
    * @exception InvocationTargetException if a target invocation error occurs
    * @exception IOException if a IO error occurs
    * @exception InvocationTargetException if a component invocation error occurs
    */
    Provider getProvider() throws IOException, InvocationTargetException;

   /**
    * Return true if this handler is a candidate for the supplied service definition.
    * @param service the service descriptor
    * @return true if this is a candidate
    * @exception RemoteException if a remote exception occurs
    */
    boolean isaCandidate( Service service ) throws RemoteException;
    
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
    * Returns the commissioned status of the handler.
    * @return TRUE if the handler has been commissioned otherwise FALSE
    * @exception RemoteException if a remote exception occurs
    */
    boolean isActive() throws RemoteException;
    
   /**
    * Return the number of instances currently under management.
    * @return the instance count.
    * @exception RemoteException if a remote exception occurs
    */
    int size() throws RemoteException;
    
}

