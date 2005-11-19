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

package net.dpml.metro.part;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * The PartHandler interface defines the a contract for an object that provides generalized
 * part loading.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Controller
{
   /**
    * Returns the identity of the object implementing this interface.
    * @return a uri identifying the object
    */
    URI getURI();

   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param part the part data structure
    * @return the management context instance
    * @exception ControlException if a part related error occurs
    */
    Context createContext( Part part ) throws ControlException;

   /**
    * Create a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param context a component part 
    * @return the classloader
    * @exception ControlException if a part related error occurs
    */
    ClassLoader createClassLoader( ClassLoader anchor, Context context ) throws ControlException;

   /**
    * Create and return a remote reference to a component handler.
    * @param context the component definition
    * @return the component handler
    * @exception Exception if a component construction error occurs
    */
    Component createComponent( Context context ) throws Exception;

   /**
    * Load a part from serialized form.  The uri is assumed to be a uri that 
    * can be transformed to a URL from which an input stream to a PartHolder 
    * can be established.  The part holder is used to identify the part handler
    * to use for part creation.  An implementation will delegate foreign part 
    * loading to an identified handler.
    *
    * @param uri the part uri
    * @return the part instance
    * @exception ControlException if a part related error occurs
    * @exception IOException if an I/O error occurs
    */
    Part loadPart( URI uri ) throws ControlException, IOException;
    
   /**
    * Load a part from serialized form.  The url identifies a part holder 
    * which is used to identify the part handler to use for part creation. 
    * An implementation will delegate foreign part loading to an identified handler.
    *
    * @param url the part url
    * @return the part instance
    * @exception ControlException if a part related error occurs
    * @exception IOException if an I/O error occurs
    */
    Part loadPart( URL url ) throws ControlException, IOException;
    
   /**
    * Load a part from a serialized object byte array. 
    * @param bytes the byte array
    * @return the part
    * @exception IOException if an I/O error occurs
    */
    Part loadPart( byte[] bytes ) throws IOException;

}
