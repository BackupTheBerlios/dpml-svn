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

package dpmlx.schema;

import java.io.Serializable;

import net.dpml.lang.Classpath;

import dpmlx.lang.PartHandler;

/**
 * Construct a part.
 */
public class PluginPartHandler implements PartHandler
{
   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    */
    public ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath )
    {
        throw new UnsupportedOperationException( "getClassLoader" );
    }

   /**
    * Instantiate a value.
    * @param classloader the implementation classloader established for the part
    * @param data the part deployment data
    * @param args supplimentary arguments
    * @exception Exception if a deployment error occurs
    */
    public Object getInstance( ClassLoader classloader, Object data, Object[] args ) throws Exception
    {
        throw new UnsupportedOperationException( "getInstance" );
    }
}
