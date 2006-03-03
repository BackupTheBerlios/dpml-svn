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

package dpmlx.schema;

import java.io.Serializable;
import java.net.URI;

import dpmlx.lang.Strategy;
import dpmlx.lang.StrategyBuilder;
import dpmlx.lang.PartDirective;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;
import net.dpml.transit.util.ElementHelper;
import net.dpml.part.StandardPartHandler;

import org.w3c.dom.TypeInfo;
import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartStrategyBuilder implements StrategyBuilder
{
    private static final PartDirective TRANSIT_DIRECTIVE = 
      new PartDirective( StandardPartHandler.PART_HANDLER_URI, null );
    
    public Strategy buildStrategy( Element element ) throws Exception
    {
        if( !"strategy".equals( element.getTagName() ) )
        {
            final String error = 
              "Invalid element name.";
            throw new IllegalArgumentException( error );
        }
        
        TypeInfo info = element.getSchemaTypeInfo();
        String type = info.getTypeName();
        if( "plugin".equals( type ) )
        {
            String classname = ElementHelper.getAttribute( element, "class" );
            Element[] elements = ElementHelper.getChildren( element, "param" );
            Value[] values = createValues( elements );
            Plugin plugin = new Plugin( classname, values );
            return new Strategy( TRANSIT_DIRECTIVE, plugin );
        }
        else if( "resource".equals( type ) )
        {
            Element urnElement = ElementHelper.getChild( element, "urn" );
            Element pathElement = ElementHelper.getChild( element, "path" );
            String urn = ElementHelper.getValue( urnElement );
            String path = ElementHelper.getValue( pathElement );
            Resource resource = new Resource( urn, path );
            return new Strategy( TRANSIT_DIRECTIVE, resource );
        }
        else
        {
            final String error = 
              "Strategy element [" 
              + type
              + "] is not recognized.";
            throw new IllegalArgumentException( error );
        }
    }
    
    private Value[] createValues( Element[] elements )
    {
        Value[] values = new Value[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = createValue( elements[i] );
        }
        return values;
    }
    
    private Value createValue( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = createValues( elements );
            return new Construct( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new Construct( classname, method, value );
        }
    }
}
