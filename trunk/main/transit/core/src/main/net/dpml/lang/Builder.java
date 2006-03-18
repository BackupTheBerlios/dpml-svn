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

import org.w3c.dom.Element;

/**
 * Interface implemented by generic builders.
 */
public interface Builder
{
   /**
    * Create an object using a supplied classloader and DOM element.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return an object
    * @exception Exception if an error occurs
    */
    Object build( ClassLoader classloader, Element element ) throws Exception;
}
