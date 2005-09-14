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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The PartHandler interface defines the a contract for an object that provides generalized
 * part loading.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public interface PartHandler extends Remote
{
   /**
    * Returns an control object using the supplied part as the construction template.
    * @param part the construction criteria
    * @return the control instance
    */
    Control loadControl( URI uri )
      throws IOException, ControlException, PartNotFoundException, 
      PartHandlerNotFoundException, DelegationException;

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  
    *
    * @return the part estracted from the part handler referenced by the uri
    */
    Part loadPart( URI uri )
        throws DelegationException, PartNotFoundException, IOException, RemoteException;

   /**
    * Load a part from a serialized object byte array. 
    * @param bytes the byte array
    * @return the part
    */
    Part loadPart( byte[] bytes ) throws IOException, RemoteException;

    Object getContent( URLConnection connection, Class[] classes ) throws IOException, RemoteException;
}
