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

import net.dpml.part.Context;

/**
 * The PartHandler interface defines the a contract for an object that provides generalized
 * part loading.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public interface PartHandler //extends Remote
{
   /**
    * Returns an control object using the supplied part uri as the construction template.
    * @param uri a uri referencing a part template
    * @return the control instance
    */
    Control loadControl( URI uri )
      throws IOException, ControlException, PartNotFoundException, 
      PartHandlerNotFoundException, DelegationException;

   /**
    * Create and return a new management context object using a supplied part uri.
    * @param uri the part uri
    * @return the management context instance
    */
    public Object newManagementContext( URI uri ) 
      throws IOException, ControlException, PartNotFoundException, 
      PartHandlerNotFoundException, DelegationException, RemoteException;

   /**
    * Create and return a new context object using a supplied part.
    * @param part the part 
    * @return the context instance
    */
    //Context getContext( Part part ) throws ControlException, RemoteException;

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  
    *
    * @return the part estracted from the part handler referenced by the uri
    */
    Part loadPart( URI uri )
        throws DelegationException, PartNotFoundException, IOException;
    
   /**
    * Load a part editor.
    * @param part the part 
    * @return the editor
    */
    PartEditor loadPartEditor( Part part ) throws PartHandlerNotFoundException;

   /**
    * Load a part from a serialized object byte array. 
    * @param bytes the byte array
    * @return the part
    */
    Part loadPart( byte[] bytes ) throws IOException;

    Object getContent( URLConnection connection, Class[] classes ) throws IOException;
}
