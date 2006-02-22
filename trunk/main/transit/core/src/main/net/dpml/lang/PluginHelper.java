/*
 * Copyright 2006 Stephen J. McConnell.
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

import java.net.URI;
import org.w3c.dom.Element;

/**
 * Interface implemented by plugins that resolve plugins from XML files.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PluginHelper
{
   /**
    * Construct a plugin using a supplied element and uri.
    *
    * @param uri the uri identifying the plugin
    * @param element the root element definining the plugin
    * @exception Exception if an error occurs
    */
    Plugin resolve( URI uri, Element element ) throws Exception;
    
}
