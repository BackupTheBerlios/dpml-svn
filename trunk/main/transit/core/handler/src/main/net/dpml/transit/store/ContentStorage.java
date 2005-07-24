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

import java.util.Properties;

/**
 * The ContentStorage defines the contract of object implementing persistent storage
 * of a content handler model.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface ContentStorage extends CodeBaseStorage
{
   /**
    * Return the content storage type identifier.
    * @return the content type immutable identifier
    */
    String getType();

   /**
    * Return the content title
    * @return the content type title
    */
    String getTitle();

   /**
    * Return the content properties.
    * @return the properties
    */
    Properties getProperties();

   /**
    * Set the content title to the supplied value.
    * @param title the content title
    */
    void setTitle( String title );

   /**
    * Set a property to the supplied value.
    * @param key the property key
    * @param value the property value
    */
    void setProperty( String key, String value );

   /**
    * Remove a property.
    * @param key the property key
    */
    void removeProperty( String key );
}
