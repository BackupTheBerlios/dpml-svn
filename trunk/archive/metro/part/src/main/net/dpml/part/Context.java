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

package net.dpml.part;

import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.part.ActivationPolicy;

/**
 * The Context interfaces is used mark a object as manageable context used in 
 * the creation of a runtime handler.
 *
 * @see PartHandler#createHandler(Context)
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Context
{
    final String PARTITION_SEPARATOR = "/";
    
   /**
    * Return the path identifying the context.  A context path commences with the
    * PARTITION_SEPARATOR character and is followed by a context name.  If the 
    * context exposed nested context objects, the path is component of context names
    * seaprated by the PARTITION_SEPARATOR as in "/main/web/handler".
    *
    * @return the context path
    */
    String getContextPath() throws RemoteException;
 
   /**
    * Get the activation policy.  If the activation policy is STARTUP, an implementation
    * a handler shall immidiately activation a runtime instance.  If the policy is on DEMAND
    * an implementation shall defer activiation until an explicit request is received.  If 
    * the policy if SYSTEM activation may occur at the discretion of an implementation.
    *
    * @return the activation policy
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    ActivationPolicy getActivationPolicy() throws RemoteException;
}

