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

/**
 * The Process interface defines a process model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Type extends Dictionary
{
   /**
    * JAR type name constant.
    */
    static final String JAR = "jar";
    
   /**
    * Plugin type name constant.
    */
    static final String PLUGIN = "plugin";
    
   /**
    * Module type name constant.
    */
    static final String MODULE = "module";
    
   /**
    * Return the name of the type.
    * @return the type name
    */
    String getName();
    
   /**
    * Return the alias association policy.
    * @return true if alias production assumed
    */
    boolean getAlias();
}
