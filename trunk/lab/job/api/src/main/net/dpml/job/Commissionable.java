/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.job;

import java.rmi.RemoteException;

/**
 * Interface implemented by objects that implement commission/decommmissioning
 * lifecycles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Commissionable
{
   /**
    * Commission the instance.
    * @exception Exception if an error occurs
    */
    void commission() throws Exception;

   /**
    * Decommission the instance.
    * @exception RemoteException if remote invocation transport error occurs
    */
    void decommission() throws RemoteException;
}
