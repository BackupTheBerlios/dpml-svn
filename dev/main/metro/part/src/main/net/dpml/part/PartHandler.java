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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.URI;

import net.dpml.part.control.Controller;

/**
 * Definition of a part handler.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface PartHandler extends Remote
{
   /**
    * Returns the uri of the handler.
    * @return the handler uri
    */
    URI getURI() throws RemoteException;

   /**
    * Load a controller given a part handler uri.
    * @return the part handler
    */
    Controller getPrimaryController( URI uri ) 
      throws PartHandlerNotFoundException, IOException, DelegationException, RemoteException;

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  
    *
    * @return the part estracted from the part handler referenced by the uri
    */
    public Part loadPart( URI uri )
        throws DelegationException, PartNotFoundException, IOException, RemoteException;

   /**
    * Load a part from a serialized object byte array. 
    * @param bytes the byte array
    * @return the part
    */
    Part loadPart( byte[] bytes ) throws IOException, RemoteException;

}
