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

import net.dpml.lang.Type;
//import net.dpml.lang.TypeBuilder;
import net.dpml.lang.Builder;
import net.dpml.lang.DefaultType;

import org.w3c.dom.TypeInfo;
import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartStrategyBuilder extends PartStrategyWriter implements Builder, StrategyBuilder //, TypeBuilder
{
    private static final PartDirective TRANSIT_DIRECTIVE = 
      new PartDirective( AbstractBuilder.LOCAL_URI );
    
    public PartStrategyBuilder()
    {
        this( null );
    }
    
    public PartStrategyBuilder( Map map )
    {
        super( map );
    }
    
    public String getID()
    {
        return "part";
    }
    
    public void writeStrategy( Writer writer, Strategy strategy, String pad ) throws IOException
    {
        Object data = strategy.getDeploymentData();
        if( data instanceof Plugin )
        {
            Plugin plugin = (Plugin) data;
            String classname = plugin.getClassname();
            writer.write( "\n  <strategy xsi:type=\"plugin\" class=\"" );
            writer.write( classname );
            writer.write( "\"/>" );
        }
        else if( data instanceof Resource )
        {
            Resource resource = (Resource) data;
            String urn = resource.getURN();
            String path = resource.getPath();
            writer.write( "\n  <strategy xsi:type=\"resource\"" );
            writer.write( " urn=\"" + urn );
            writer.write( "\" path=\"" + path );
            writer.write( "\"/>" );
        }
        else
        {
            final String error = 
              "Unsupported strategy datatype: " + data.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }
    
   /*
    public Type buildType( ClassLoader classloader, Element element ) throws Exception
    {
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        Strategy strategy = buildStrategy( classloader, element );
        return new DefaultType( "part", alias, strategy ); 
    }
    */
    
    public Object build( ClassLoader classloader, Element element ) throws Exception
    {
        return buildStrategy( classloader, element );
    }
    
    public Strategy buildStrategy( ClassLoader classloader, Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String type = info.getTypeName();
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        if( "plugin".equals( type ) )
        {
            String classname = ElementHelper.getAttribute( element, "class" );
            Element[] elements = ElementHelper.getChildren( element, "param" );
            Value[] values = buildValues( elements );
            Plugin plugin = new Plugin( classname, values );
            return new Strategy( AbstractBuilder.LOCAL_URI, TRANSIT_DIRECTIVE, plugin, alias );
        }
        else if( "resource".equals( type ) )
        {
            String urn = ElementHelper.getAttribute( element, "urn" );
            String path = ElementHelper.getAttribute( element, "path" );
            Resource resource = new Resource( urn, path );
            return new Strategy( AbstractBuilder.LOCAL_URI, TRANSIT_DIRECTIVE, resource, alias );
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
}
