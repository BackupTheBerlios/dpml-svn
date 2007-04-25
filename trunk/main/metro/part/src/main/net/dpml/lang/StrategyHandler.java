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

import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Interace implemented by part strategy handlers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface StrategyHandler
{
   /**
    * Creation of a new strategy handler for the supplied implementation class.
    * @param c the component implementation class
    * @return a new strategy
    * @exception IOException if an I/O error occurs
    */
    Strategy newStrategy( Class<?> c ) throws IOException;

   /**
    * Creation of a new strategy handler for the supplied implementation class.
    * @param c the component implementation class
    * @param name the component name
    * @return a new strategy
    * @exception IOException if an I/O error occurs
    */
    Strategy newStrategy( Class<?> c, String name ) throws IOException;
    
   /**
    * Construct a new strategy using a supplied element and value resolver.
    * @param classloader the classloader
    * @param element the DOM element definining the deployment strategy
    * @param resolver symbolic property resolver
    * @param partition the assigned partition
    * @param query the query 
    * @param validate if true validate the strategy integrity
    * @return the strategy
    * @exception IOException if an I/O error occurs
    */
    Strategy build( 
      ClassLoader classloader, Element element, Resolver resolver, String partition, 
      String query, boolean validate ) throws IOException;
}
