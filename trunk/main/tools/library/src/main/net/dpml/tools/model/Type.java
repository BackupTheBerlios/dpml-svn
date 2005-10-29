/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.model;

import java.net.URI;

/**
 * The Process interface defines a process model.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Type extends Dictionary
{
    static final String JAR = "jar";
    static final String PLUGIN = "plugin";
    static final String MODULE = "module";
    
   /**
    * Return the name of the type.
    */
    String getName();
    
   /**
    * Return the alias association policy.
    */
    boolean getAlias();
}
