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
import java.util.Map;

/**
 * A object resolvable from primitive and symbolic arguments.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Value
{
   /**
    * Return TRUE if the value is a compound else FALSE.
    * @return TRUE if this ia a compound value construct
    */
    boolean isCompound();

   /**
    * Return the method name to be applied to the target object.
    * @return the method name
    */
    String getMethodName();

   /**
    * Return the set of nested values within this value.
    * @return the nested values array
    */
    Value[] getValues();

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    String getBaseValue();

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    String getTargetExpression();

   /**
    * Resolve an instance from the value using the context classloader.
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    Object resolve() throws Exception;
    
   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param map the context map
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    Object resolve( Map map ) throws Exception;

}
