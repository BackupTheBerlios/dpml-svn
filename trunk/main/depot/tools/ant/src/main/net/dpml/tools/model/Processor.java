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

import net.dpml.library.model.Dictionary;

/**
 * The Processor interface defines a process model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Processor extends Dictionary
{
   /**
    * Return the name of the process.
    * @return the processor name
    */
    String getName();
    
   /**
    * Return the processor codebase uri.
    * @return the processor codebase uri
    */
    URI getCodeBaseURI();

   /**
    * Return the processor classname.
    * @return a possibly null classname
    */
    String getClassname();
    
   /**
    * Return an array of dependent process names declared by the process type.
    * @return the process names that this process is depends on
    */
    String[] getDepends();

}
