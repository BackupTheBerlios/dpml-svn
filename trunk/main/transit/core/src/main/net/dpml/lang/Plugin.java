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
import java.io.OutputStream;

/**
 * A Plugin class contains immutable data about a plugin based on a descriptor resolved
 * from a 'plugin' artifact.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Plugin
{
   /**
    * Return the plugin title.
    * @return the plugin title
    */
    String getTitle();
    
   /**
    * Return the plugin description.
    * @return the plugin description
    */
    String getDescription();
    
   /**
    * Return the plugin version.
    * @return the plugin version identifier
    */
    Version getVersion();
    
   /**
    * Return the uri used to establish the plugin.
    * @return the plugin uri
    */
    URI getURI();
    
   /**
    * Return the plugin strategy.
    * @return the plugin strategy
    */
    Strategy getStrategy();
    
   /**
    * Return the classpath definition.
    * @return the classpath
    */
    Classpath getClasspath();

   /**
    * Write an XML representation of the plugin to the output stream.
    * @param output the output stream
    * @exception Exception if an error ocucrs during externalization
    */
    void write( OutputStream output ) throws Exception;
}
