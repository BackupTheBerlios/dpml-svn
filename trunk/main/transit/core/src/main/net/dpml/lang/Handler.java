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

import java.util.Properties;

/**
 * Interface implemented by plugin handlers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Handler
{
   /**
    * Instantiate the plugin.
    *
    * @param classloader the classloader
    * @param properties plugin strategy properties
    * @param args commandline arguments
    * @return the instance
    * @exception Exception if an error occurs
    */
    Object getPlugin( 
      ClassLoader classloader, Properties properties, Object[] args  ) throws Exception;
    
   /**
    * Get a plugin class.
    *
    * @param classloader the plugin classloader
    * @param properties the supplimentary properties
    * @return the plugin class
    * @exception Exception if an error occurs
    */
    Class getPluginClass( ClassLoader classloader, Properties properties ) throws Exception;
}
