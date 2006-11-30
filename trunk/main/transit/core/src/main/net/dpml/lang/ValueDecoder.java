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

import net.dpml.util.ElementHelper;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Utility used to decode values from DOM elements.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ValueDecoder
{
   /**
    * Build an array of values for the supplied element array.
    * @param elements the elements
    * @return the resolved values
    */
    public Value[] decodeValues( Element[] elements )
    {
        return decodeValues( elements, null );
    }
    
   /**
    * Build an array of values for the supplied element array.
    * @param elements the elements
    * @return the resolved values
    */
    public Value[] decodeValues( Element[] elements, Resolver resolver )
    {
        Value[] values = new Value[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = decodeValue( elements[i], resolver );
        }
        return values;
    }
    
   /**
    * Build a single value instance from a supplied element.
    * @param element the element
    * @return the resolved value
    */
    public Value decodeValue( Element element )
    {
        return decodeValue( element, null );
    }
    
   /**
    * Build a single value instance from a supplied element.
    * @param element the element
    * @return the resolved value
    */
    public Value decodeValue( Element element, Resolver resolver )
    {
        String classname = ElementHelper.getAttribute( element, "class", null, resolver );
        String method = ElementHelper.getAttribute( element, "method", null, resolver );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = decodeValues( elements, resolver );
            return new Construct( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value", null, resolver );
            return new Construct( classname, method, value );
        }
    }
}
