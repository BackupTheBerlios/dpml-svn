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

package net.dpml.lang;

import java.util.Map;

/**
 * A object resolvable from primitive and symbolic arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Value
{
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

   /**
    * Resolve an instance from the value using a supplied isolation policy. 
    *
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    Object resolve( boolean isolate ) throws Exception;

   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param map the context map
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    Object resolve( Map map, boolean isolate ) throws Exception;

   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param classname the default classname
    * @param map the context map
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    Object resolve( String classname, Map map, boolean isolate ) throws Exception;

}
