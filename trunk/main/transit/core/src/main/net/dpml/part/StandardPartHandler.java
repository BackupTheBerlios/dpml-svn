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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.io.IOException;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;

import net.dpml.transit.Artifact;
import net.dpml.transit.Construct;
import net.dpml.transit.StandardClassLoader;
import net.dpml.transit.Transit;
import net.dpml.transit.Value;

/**
 * Construct a part.
 */
public class StandardPartHandler implements PartHandler
{
   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    */
    public ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath ) throws IOException
    {
        return Transit.getInstance().getRepository().createClassLoader( anchor, null, classpath );
    }
    
   /**
    * Instantiate a value.
    * @param classloader the implementation classloader established for the part
    * @param data the part deployment data
    * @param args supplimentary arguments
    * @exception Exception if a deployment error occurs
    */
    public Object getInstance( ClassLoader anchor, Classpath classpath, Object data, Object[] args ) throws Exception
    {
        ClassLoader classloader = getClassLoader( anchor, classpath );
        if( data instanceof Plugin )
        {
            Plugin plugin = (Plugin) data;
            String classname = plugin.getClassname();
            Value[] values = plugin.getValues();
            ClassLoader context = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( classloader );
            try
            {
                Object[] params = Construct.getArgs( null, values, args );
                Class c = classloader.loadClass( classname );
                return Transit.getInstance().getRepository().instantiate( c, params );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( context );
            }
        }
        else
        {
            throw new UnsupportedOperationException( "getInstance/" + data.getClass().getName() );
        }
    }
}
