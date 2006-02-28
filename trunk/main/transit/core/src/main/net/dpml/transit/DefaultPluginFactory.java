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

package net.dpml.transit;

import java.net.URI;

import net.dpml.lang.Plugin;
import net.dpml.lang.Strategy;
import net.dpml.lang.Classpath;
import net.dpml.lang.PluginFactory;

/**
 * Default plugin factory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultPluginFactory implements PluginFactory
{
   /**
    * Construct a new plugin description instance using a supplied arguments
    *
    * @param uri the uri identifying the plugin
    * @param element the root element definining the plugin
    * @exception Exception if an error occurs
    */
    public Plugin newPlugin( 
      String title, String description, URI uri, Strategy strategy, Classpath classpath ) 
      throws Exception
    {
        return new DefaultPlugin( title, description, uri, strategy, classpath );
    }
}

