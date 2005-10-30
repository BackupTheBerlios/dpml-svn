/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.tools.tasks;


/**
 * The plugin task handles the establishment of ant tasks, listeners, and antlibs derived
 * from a classloader established by the tools sub-system.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PluginTask extends net.dpml.transit.tools.PluginTask
{
    public void init()
    {
        super.init();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
    }
}
