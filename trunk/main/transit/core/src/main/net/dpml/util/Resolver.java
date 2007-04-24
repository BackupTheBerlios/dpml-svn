/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package net.dpml.util;

/**
 * Interace implemented by a value (key, ref, and property) resolver.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Resolver
{
   /**
    * Return a property value.
    * @param key the property key
    * @return the property value
    */
    String getProperty( String key );
    
   /**
    * Return a property value.
    * @param key the property key
    * @param value the default value
    * @return the property value
    */
    String getProperty( String key, String value );
    
   /**
    * Symbolic expansion of a supplied value.
    * Replace any occurances of ${[key]} with the value of the property
    * assigned to the [key] in system properties.
    * @param value a string containing possibly multiple ${[value]} sequences
    * @return the expanded string
    */
    String resolve( String value );
}
