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

package net.dpml.metro.builder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.XMLConstants;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;

import net.dpml.lang.Value;
import net.dpml.lang.Construct;
import net.dpml.transit.util.ElementHelper;

import net.dpml.part.Strategy;
import net.dpml.part.StrategyBuilder;
import net.dpml.part.PartDirective;
import net.dpml.lang.BuilderException;
import net.dpml.part.ValueBuilder;
import net.dpml.lang.Type;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyWriter extends ValueBuilder
{
    final String CONTROLLER_URI = "@CONTROLLER-URI@";
    final String BUILDER_URI = "@BUILDER-URI@";
    final String COMPONENT_XSD_URI = "@COMPONENT-XSD-URI@";
    
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String TYPE_SCHEMA_URN = "@TYPE-XSD-URI@";
    private static final String STATE_SCHEMA_URN = "@STATE-XSD-URI@";
    private static final String PART_SCHEMA_URN = "@PART-XSD-URI@";
    private static final String COMPONENT_SCHEMA_URN = "@COMPONENT-XSD-URI@";
    
    private static final String PARTIAL_COMPONENT_HEADER = 
      "<component xmlns=\"" 
      + COMPONENT_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\"\n    xmlns:part=\"" 
      + PART_SCHEMA_URN
      + "\"\n    xmlns:type=\"" 
      + TYPE_SCHEMA_URN
      + "\"\n    xmlns:component=\"" 
      + COMPONENT_SCHEMA_URN
      + "\"";
    
    final ComponentBuilder BUILDER = new ComponentBuilder();
    
    public ComponentStrategyWriter( Map map )
    {
        super( map );
    }

    public void writeStrategy( 
      Writer writer, Strategy strategy, String pad ) throws IOException
    {
        Object data = strategy.getDeploymentData();
        if( data instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) data;
            writer.write( "\n" + pad + "<component xmlns=\"" + COMPONENT_SCHEMA_URN + "\"" );
            writer.write( "\n" + pad + "  " );
            BUILDER.writeAttributes( writer, directive, pad + "   " );
            BUILDER.writeBody( writer, directive, pad + "  " );
            writer.write( "\n" + pad + "</component>" );
        }
        else
        {
            final String error = 
              "Strategy argument ["
              + data.getClass().getName()
              + "] is not an instance of " 
              + ComponentDirective.class.getName()
              + ".";
            throw new IllegalArgumentException( error );
        }
    }
}
