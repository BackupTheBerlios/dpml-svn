/*
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

import net.dpml.metro.info.Type;

/**
 * The contract for builders that create component types.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface TypeBuilder
{
   /**
    * Build a part type.
    * @param classloader the classloader
    * @return the serializable part type.
    * @exception IntrospectionException if a introspection occurs during type construction
    * @exception IOException if an I/O exception occurs
    */
    Type buildType( ClassLoader classloader ) 
       throws IntrospectionException, IOException;

}
