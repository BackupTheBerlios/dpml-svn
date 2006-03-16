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

import net.dpml.lang.Value;

/**
 * The contract for builders that create value datatypes.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ValueBuilder
{
   /**
    * Build a value datastructure.
    * @param classloader the working classloader
    * @return the serializable value descriptor
    */
    Value buildValue( ClassLoader classloader );
    
   /**
    * Return the base classname.
    * @param classloader the working classloader
    * @return the target class
    */
    Class getTargetClass( ClassLoader classloader );
}
