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

package net.dpml.composition.builder;

import java.io.IOException;
import java.beans.IntrospectionException;

import net.dpml.composition.info.PartReference;
import net.dpml.composition.info.Type;

/**
 * The contract for builders that create component part.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface PartReferenceBuilder
{
   /**
    * Return the key identifying the part that this builder is building.
    */
    String getKey();

   /**
    * Build the part.
    * @param classloader the classloader to use if type creation is required
    * @return the serializable part
    * @exception Exception if a construction error occurs
    */
    PartReference buildPartReference( ClassLoader classloader, Type type ) 
       throws IntrospectionException, IOException, ClassNotFoundException;
}
