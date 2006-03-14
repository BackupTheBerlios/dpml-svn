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

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.TypeInfo;
import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ValueBuilder extends ValueWriter
{
    //public ValueBuilder()
    //{
    //    this( null );
    //}
    
    public ValueBuilder( Map map )
    {
        super( map );
    }
    
    protected Value[] buildValues( Element[] elements )
    {
        Value[] values = new Value[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = buildValue( elements[i] );
        }
        return values;
    }
    
    protected Value buildValue( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = buildValues( elements );
            return new Construct( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new Construct( classname, method, value );
        }
    }
}
