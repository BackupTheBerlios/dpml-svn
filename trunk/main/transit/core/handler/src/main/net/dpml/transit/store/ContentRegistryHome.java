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

/**
 * Interface implemented by classes providing content handler configuration
 * storage.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface ContentRegistryHome extends CodeBaseStorage
{
   /**
    * Return an aray of the content storage units initally assigned under
    * the content registry storage home.
    * 
    * @return an array of content storage units
    */
    ContentStorage[] getInitialContentStores();

   /**
    * Create a content storage unit for the supplied content type.
    * @param type the content type
    * @return the content storage unit
    */
    ContentStorage createContentStorage( String type );

   /**
    * Create a content storage unit using a supplied type, title and uri.
    * @param type the content type
    * @param title the content type title
    * @param uri a plugin codebase uri
    * @return the content storage unit
    */
    ContentStorage createContentStorage( String type, String title, URI uri );

}
