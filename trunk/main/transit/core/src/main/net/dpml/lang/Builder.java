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

import java.io.IOException;

import net.dpml.lang.Classpath;

import org.w3c.dom.Element;

/**
 * Interace implemented by part strategy builders.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Builder
{
   /**
    * Construct a new part.
    * @param info the part information descriptor
    * @param classpath the part classpath descriptor
    * @param strategy the DOM element definining the deployment strategy
    * @return the part definition
    * @exception IOException if an I/O error occurs
    */
    Part build( Info info, Classpath classpath, Element strategy ) throws IOException;
}
