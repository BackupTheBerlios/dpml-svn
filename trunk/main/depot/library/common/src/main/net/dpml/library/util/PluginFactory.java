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

package net.dpml.library.util;

import java.io.File;

import net.dpml.lang.Plugin;

import net.dpml.library.model.Resource;

/**
 * Interface implemented by plugins that provide plugin building functionality.
 * Implementations that load plugin factoryies must supply the target Resource
 * as a plugin constructor argument.  Factory implementation shall construct 
 * plugin defintions using the supplied resource as the reference for the 
 * classpath dependencies.  Suppliementary properties may be aquired using 
 * the Type returned from the Resource.getType( "plugin" ) operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PluginFactory
{
   /**
    * Build the plugin definition.
    * @exception exception if a build related error occurs
    */
    Plugin build( File basedir, Resource resource ) throws Exception;
}
