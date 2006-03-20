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
import java.io.Writer;

import javax.xml.XMLConstants;

import net.dpml.metro.data.ComponentDirective;

import net.dpml.part.Strategy;

import net.dpml.lang.Encoder;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyEncoder extends ComponentConstants implements Encoder
{
    static final String CONTROLLER_URI = "@CONTROLLER-URI@";
    static final String BUILDER_URI = "@COMPONENT_DECODER-URI@";
    
    private static final String COMPONENT_XSD_URI = "@COMPONENT-XSD-URI@";
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
    
    static final ComponentEncoder COMPONENT_ENCODER = new ComponentEncoder();
    
   /**
    * Write a component strategy to an output stream writer.
    * @param writer the output stream writer
    * @param object the object to encode
    * @param pad the character offset to apply
    * @exception IOException if an IO error occurs
    */
    public void encode( Writer writer, Object object, String pad ) throws IOException
    {
        if( object instanceof Strategy )
        {
            writeStrategy( writer, (Strategy) object, pad );
        }
        else
        {
            final String error = 
              "Encoding subject is not recognized."
              + "\nObject: " + object.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Write a component strategy to an output stream writer.
    * @param writer the output stream writer
    * @param strategy the strategy instance
    * @param pad the character offset to apply
    * @exception IOException if an IO error occurs
    */
    public void writeStrategy( 
      Writer writer, Strategy strategy, String pad ) throws IOException
    {
        Object data = strategy.getDeploymentData();
        if( data instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) data;
            writer.write( "\n" + pad + "<component xmlns=\"" + COMPONENT_SCHEMA_URN + "\"" );
            writer.write( "\n" + pad + "  " );
            COMPONENT_ENCODER.writeAttributes( writer, directive, pad + "   " );
            COMPONENT_ENCODER.writeBody( writer, directive, pad + "  " );
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
