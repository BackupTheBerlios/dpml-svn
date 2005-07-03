/*
 * Copyright 2004 Stephen J. McConnell.
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

package tutorial.plugin.controller;

import java.net.URI;
import java.net.URL;

import net.dpml.transit.repository.Repository;
import net.dpml.transit.repository.StandardLoader;

/**
 * A simple plugin enhanced to include cli handling.
 *
 * @author <a href="mailto:mcconnell@dpml.net">DPML Development</a>
 */
public class ControllerPlugin
{
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * A plugin deployed by the transit plugin tool that controls the 
    * establishment of another plugin.
    *
    * @param args the commandline arguments
    */
    public ControllerPlugin( String[] args ) throws Exception
    {
        //
        // create a uri for the plugin we intend to deploy
        //

        String spec = "@PLUGIN-URI@";
        URI uri = new URI( spec );

        //
        // create a transit repository manager
        //

        Repository repository = new StandardLoader();
        ClassLoader classloader = getClass().getClassLoader();

        //
        // deploy the plugin
        //

        Object[] params = new Object[]{ args };
        Object plugin = repository.getPlugin( classloader, uri, params );

        // ...

    }
}

