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

import dpml.util.ElementHelper;

import java.io.IOException;

import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Antlib strategy handler implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AntlibStrategyHandler implements StrategyHandler
{
   /**
    * Antlib strategy namespace constant.
    */
    public static final String NAMESPACE = "dpml:antlib";
    
   /**
    * Creation of a new antlib strategy.
    * @param c the target class
    * @return the strategy instance
    * @exception IOException if an IO error occurs
    */
    public Strategy newStrategy( Class<?> c ) throws IOException
    {
        return newStrategy( c, null );
    }
    
   /**
    * Creation of a new antlib strategy.
    * @param c the target class
    * @param name the name to assign to the strategy
    * @return the strategy instance
    * @exception IOException if an IO error occurs
    */
    public Strategy newStrategy( Class<?> c, String name ) throws IOException
    {
        throw new UnsupportedOperationException( "newStrategy" );
    }
    
   /**
    * Construct a strategy definition using a supplied element and value resolver.
    * @param classloader the classloader
    * @param element the DOM element definining the deployment strategy
    * @param resolver symbolic property resolver
    * @param partition the enclosing partition
    * @param query the query fragment (ignored)
    * @param validate the validation policy
    * @return the strategy definition
    * @exception IOException if an I/O error occurs
    */
    public Strategy build( 
      ClassLoader classloader, Element element, Resolver resolver, String partition, 
      String query, boolean validate ) throws IOException
    {
        String urn = ElementHelper.getAttribute( element, "urn", null, resolver );
        String path = ElementHelper.getAttribute( element, "path", null, resolver );
        return new AntlibStrategy( classloader, urn, path );
    }
}
