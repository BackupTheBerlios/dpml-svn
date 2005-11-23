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

package net.dpml.transit.model;

import java.rmi.RemoteException;

import net.dpml.transit.Logger;
import net.dpml.transit.store.CodeBaseStorage;

/**
 * Default implementation of a pluggable repository model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultRepositoryModel extends DisposableCodeBaseModel implements RepositoryModel
{
   /**
    * Creation of a new repository service codebase model.
    * @param logger the assinged logging channel
    * @param home the codebase storage unit
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultRepositoryModel( Logger logger, CodeBaseStorage home ) 
      throws RemoteException
    {
        super( logger, home );
    }
}
