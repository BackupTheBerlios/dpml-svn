/*
 * Copyright 2005 Stephen J. McConnell
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

package dpml.library;

import java.util.Properties;

/**
 * The Dictonary interface exposes operations dealing with named properties.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Dictionary
{
   /**
    * Return the property names associated with the dictionary.
    * @return the array of property names
    */
    String[] getPropertyNames();
    
   /**
    * Return the local property names associated with the dictionary.
    * @return the array of local property names
    */
    String[] getLocalPropertyNames();

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
    * Return an boolean property value.
    * @param key the property key
    * @param value the default value
    * @return the property value as an boolean
    */
    boolean getBooleanProperty( String key, boolean value );
    
   /**
    * Return an integer property value.
    * @param key the property key
    * @param value the default value
    * @return the property value as an integer
    */
    int getIntegerProperty( String key, int value );

   /**
    * Evaluate and expand any symbolic references in the supplied property set.
    * @param properties the properties to resolve
    * @return the resolved properties
    */
    Properties resolveProperties( Properties properties );
    
   /**
    * Evaluate and expand any symbolic references in the supplied value.
    * @param value the value to resolve
    * @return the resolved value
    */
    String resolve( String value );
}
