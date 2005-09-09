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

package net.dpml.transit.model;

import java.io.Serializable;

/**
 * A object resolvable from primative arguments.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Value extends Serializable
{
   /**
    * Return the classname of the type that this value implements.
    * @return the type classname
    */
    String getTypeClassname();

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    String getBaseClassname();

   /**
    * Resolve an instance from the value.
    * @return the resolved instance
    */
    Object resolve();

   /**
    * Resolve an instance from the value.
    * @param classloader the classloader 
    * @return the resolved instance
    */
    Object resolve( ClassLoader classloader );

   /**
    * Resolve the value base class using the supplied classloader.
    * @param classloader the classloader
    * @return the resolved class
    */
    Class getBaseClass( ClassLoader classloader );

   /**
    * Resolve the value type class using the context classloader.
    * @return the resolved class
    */
    Class getTypeClass();

   /**
    * Resolve the value type class using the supplied classloader.
    * @param classloader the classloader
    * @return the resolved class
    */
    Class getTypeClass( ClassLoader classloader );
}
