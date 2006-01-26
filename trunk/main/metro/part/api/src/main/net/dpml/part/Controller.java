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

/**
 * The Controller interface defines the a contract for an object that provides generalized
 * part loading.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Controller
{
   /**
    * Static default controller.
    */
    static final Controller STANDARD = InitialContext.createController();

   /**
    * Returns the identity of the object implementing this interface.
    * @return a uri identifying the object
    */
    URI getURI();

   /**
    * Load a directive from serialized form.
    *
    * @param uri the directive uri
    * @return the directive
    * @exception ControlException if a direction construction error ocurrs
    * @exception IOException if an I/O error occurs
    */
    Directive loadDirective( URI uri ) throws ControlException, IOException;
    
   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param directive the part data structure
    * @return the management context
    * @exception ControlException if a part related error occurs
    */
    Model createModel( Directive directive ) throws ControlException;

   /**
    * Create and return a new management context using the supplied directive uri.
    *
    * @param uri a uri identifying a deployment directive
    * @return the management context model
    * @exception ControlException if an error occurs
    * @exception IOException if an I/O error occurs
    */
    Model createModel( URI uri ) throws ControlException, IOException;

   /**
    * Create and return a remote reference to a component handler.
    * @param uri a uri identifying a deployment directive
    * @return the component handler
    * @exception Exception if an error occurs
    */
    Component createComponent( URI uri ) throws Exception;

   /**
    * Create and return a remote reference to a component handler.
    * @param model the management context
    * @return the component handler
    * @exception Exception if a component construction error occurs
    */
    Component createComponent( Model model ) throws Exception;

   /**
    * Create a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param model a component model 
    * @return the classloader
    * @exception ControlException if a part related error occurs
    */
    ClassLoader createClassLoader( ClassLoader anchor, Model model ) throws ControlException;

}
