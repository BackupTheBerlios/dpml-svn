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

package org.acme;

import java.net.URI;
import java.awt.Color;

/**
 * Sample object used in test case.  Contains a number of inner context classes
 * used to evaluate strict and non-strict context evaluation strategies and 
 * general part instantiation.
 */
public class Sample
{
   /**
    * Traditional context interface definition with a nested context
    * interface as a return type and parameter to one of the entries.
    */
    public interface Context
    {
       /**
        * @return a message
        */
        String getMessage();
        
       /**
        * @param uri the default value
        * @return a uri
        */
        URI getResourceURI( URI uri );
        
       /**
        * @param value the default value
        * @return a nested context object
        */
        AnotherContext getExternal( AnotherContext value );
    }
    
   /**
    * A context defintion declared as a context using the <tt>ContextDef</tt>
    * annotation.
    */
    @net.dpml.annotation.Context
    public interface AnotherContext
    {
       /**
        * @param color the default value
        * @return a color
        */
        Color getColor( Color color );
    }
    
   /**
    * Constructor that takes a single context argument as the initial state.
    * @param context a classic context instance
    */
    public Sample( Context context )
    {
        System.out.println( "  message: " + context.getMessage() );
        System.out.println( "  resourceURI: " + context.getResourceURI( null ) );
        AnotherContext external = context.getExternal( null );
        if( null != external )
        {
            System.out.println( "  external.color: " + external.getColor( null ) );
        }
    }
    
   /**
    * Internal class used to validate that a inner context interface 
    * marked as non-a-context using the <tt>ContextDef</tt> annotion is 
    * correctly recognized as such.
    */
    public static class Negation
    {
       /**
        * A non-context interface that happens to be named Context.
        */
        @net.dpml.annotation.Context( false )
        public interface Context
        {
        }
    }
}