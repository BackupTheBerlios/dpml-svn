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

package net.dpml.metro.tools;

import java.io.IOException;
import java.beans.IntrospectionException;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Type;

/**
 * The contract for builders that create component part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PartReferenceBuilder
{
   /**
    * Return the key identifying the part that this builder is building.
    * @return the part key
    */
    String getKey();

   /**
    * Build the part.
    * @param classloader the classloader to use if type creation is required
    * @param type the underlying part type
    * @return th part reference
    * @exception IntrospectionException if a class introspection error occurs
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if a referenced class cannot be found
    */
    PartReference buildPartReference( ClassLoader classloader, Type type ) 
       throws IntrospectionException, IOException, ClassNotFoundException;
}
