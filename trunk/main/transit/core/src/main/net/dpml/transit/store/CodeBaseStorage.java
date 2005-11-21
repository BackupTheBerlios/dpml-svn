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

package net.dpml.transit.store;

import java.net.URI;

import net.dpml.transit.model.Value;

/**
 * The CodeBaseStorage is an interface implemented by objects maintaining
 * the persistent configuration of a codebase model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CodeBaseStorage
{
   /**
    * Returns the codebase storage identifier.
    * 
    * @return the immutable host model identifier
    */
    String getID();

   /**
    * Return the URI identifying a codebase.  Typically this value returned from 
    * this operation identifes a plugin uses as a system extension or customization 
    * point.
    *
    * @return the codebase uri
    */
    URI getCodeBaseURI();

   /**
    * Set the codebase uri.
    * @param uri the uri identifying the codebase for a system extension
    */
    void setCodeBaseURI( URI uri );

   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    */
    Value[] getParameters();

   /**
    * Set the array of values assigned to the codebase model for use
    * as plugin constructor parameter arguments.
    *
    * @param values the array of values
    */
    void setParameters( Value[] values );

}
