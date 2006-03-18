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

package net.dpml.part;

import java.io.IOException;

import net.dpml.lang.Classpath;

/**
 * Interface implemented by part runtime handlers. Handler are identified
 * by the uri returned from <tt>Part.getStrategy().getControllerURI()</tt>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PartHandler
{
   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    * @return the new classloader
    * @exception IOException if an IO error occurs during classpath evaluation
    */
    ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath ) throws IOException;

   /**
    * Instantiate a value.
    * @param anchor the anchor classloader
    * @param classpath the part classpath
    * @param data the part deployment data
    * @param args supplimentary arguments
    * @return the resolved instance
    * @exception Exception if a deployment error occurs
    */
    Object getInstance( ClassLoader anchor, Classpath classpath, Object data, Object[] args ) throws Exception;
    
}
