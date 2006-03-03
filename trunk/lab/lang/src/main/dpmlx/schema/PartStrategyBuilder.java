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

import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.util.ElementHelper;

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
    private static final PartDirective TRANSIT_DIRECTIVE = createTransitDirective();
    
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
            ValueDirective[] values = createValueDirectives( elements );
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
    
    private ValueDirective[] createValueDirectives( Element[] elements )
    {
        ValueDirective[] values = new ValueDirective[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = createValueDirective( elements[i] );
        }
        return values;
    }
    
    private ValueDirective createValueDirective( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            ValueDirective[] values = createValueDirectives( elements );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new ValueDirective( classname, method, value );
        }
    }
    
    private static PartDirective createTransitDirective()
    {
        try
        {
            URI uri = new URI( "internal:transit" );
            return new PartDirective( uri, null );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
