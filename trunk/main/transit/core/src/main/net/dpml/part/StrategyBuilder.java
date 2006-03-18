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

import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.Element;

/**
 * Construct an Strategy instance from a DOM Element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface StrategyBuilder
{
   /**
    * Build a strategy from a supplied DOM element.
    * @param classloader the classloader
    * @param element the strategy element
    * @return the resolved strategy
    * @exception Exception if an error occurs
    */
    Strategy buildStrategy( ClassLoader classloader, Element element ) throws Exception;
    
   /**
    * Externalize a strategy.
    * @param writer the output stream writer
    * @param strategy the strategy
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    void writeStrategy( Writer writer, Strategy strategy, String pad ) throws IOException;
}
