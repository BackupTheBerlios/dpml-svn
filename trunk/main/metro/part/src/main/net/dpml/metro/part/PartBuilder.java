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
import java.beans.IntrospectionException;

/**
 * The contract for builders that create component parts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PartBuilder
{
   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return the part handler uri
    */
    URI getPartHandlerURI();

   /**
    * Build the part.
    * @param classloader the classloader to use if type creation is required
    * @return the serializable part
    * @exception IntrospectionException if an error occurs during the 
    *  part introspection phase
    * @exception IOException if a IO error occurs
    * @exception ClassNotFoundException if the part class could not be found
    */
    Part buildPart( ClassLoader classloader ) 
       throws IntrospectionException, IOException, ClassNotFoundException;
}
