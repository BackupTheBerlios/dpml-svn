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
import java.beans.IntrospectionException;

import net.dpml.part.Part;

/**
 * The contract for builders that create component parts.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface PartBuilder
{

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return a strategy uri
    */
    URI getPartHandlerURI();

   /**
    * Build the part.
    * @param classloader the classloader to use if type creation is required
    * @return the serializable part
    * @exception Exception if a construction error occurs
    */
    Part buildPart( ClassLoader classloader ) 
       throws IntrospectionException, IOException, ClassNotFoundException;
}
