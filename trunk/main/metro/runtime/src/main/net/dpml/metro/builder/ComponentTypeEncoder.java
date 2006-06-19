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

import javax.xml.XMLConstants;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Descriptor;

import net.dpml.state.State;
import net.dpml.state.impl.StateEncoder;

/**
 * Type builder.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentTypeEncoder
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String PART_SCHEMA_URN = "@PART-XSD-URI@";
    private static final String COMPONENT_SCHEMA_URN = "@COMPONENT-XSD-URI@";
    private static final String TYPE_SCHEMA_URN = "@TYPE-XSD-URI@";
    
    private static final StateEncoder STATE_GRAPH_ENCODER = new StateEncoder();
    
    static final ComponentEncoder COMPONENT_ENCODER = new ComponentEncoder();
    
    private static final String HEADER = 
      "<type xmlns=\"" 
      + TYPE_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\"\n    xmlns:part=\"" 
      + PART_SCHEMA_URN
      + "\"\n    xmlns:type=\"" 
      + TYPE_SCHEMA_URN
      + "\"\n    xmlns:component=\"" 
      + COMPONENT_SCHEMA_URN
      + "\">";

    private static final String FOOTER = "</type>";
    
   /**
    * Write the supplied type to an output stream.
    * @param type the type descriptor
    * @param output the output stream
    * @exception IOException if an IO error occurs
    */
    public void export( Type type, OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writer.write( XML_HEADER );
        writer.write( "\n\n" );
        writer.write( HEADER );
        writer.write( "\n" );
        writeBody( writer, type, "  " );
        writer.write( "\n" );
        writer.write( FOOTER );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }
    
    private void writeBody( Writer writer, Type type, String pad ) throws IOException
    {
        writeTypeInfo( writer, type.getInfo(), pad );
        writeTypeServices( writer, type.getServiceDescriptors(), pad );
        writeTypeContext( writer, type.getContextDescriptor(), pad );
        writeTypeCategories( writer, type.getCategoryDescriptors(), pad );
        writeTypeState( writer, type.getStateGraph(), pad );
        writeTypeParts( writer, type.getPartReferences(), pad );
    }
    
    private void writeTypeInfo( Writer writer, InfoDescriptor info, String pad ) throws IOException
    {
        writer.write( "\n" + pad + "<info" );
        writer.write( " name=\"" + info.getName() );
        writer.write( "\"\n" + pad + "    class=\"" + info.getClassname() );
        writer.write( "\"\n" + pad + "    version=\"" + info.getVersion() );
        writer.write( "\"\n" + pad + "    lifestyle=\"" + info.getLifestylePolicy().getName().toLowerCase() );
        writer.write( "\"\n" + pad + "    collection=\"" + info.getCollectionPolicy().getName().toLowerCase() );
        writer.write( "\"\n" + pad + "    threadsafe=\"" + info.getThreadSafePolicy().getName().toLowerCase() );
        writer.write( "\"" );
        if( info.getAttributeNames().length > 0 )
        {
            writer.write( ">" );
            writeAttributes( writer, info, pad + "  " );
            writer.write( "\n" + pad + "</info>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }

    private void writeTypeServices( Writer writer, ServiceDescriptor[] services, String pad ) throws IOException
    {
        if( services.length == 0 )
        {
            return;
        }
        else
        {
            writer.write( "\n" + pad + "<services>" );
            for( int i=0; i<services.length; i++ )
            {
                ServiceDescriptor service = services[i];
                writer.write( "\n" + pad + "  <service" );
                writer.write( " class=\"" + service.getClassname() );
                writer.write( "\" version=\"" + service.getVersion() );
                writer.write( "\"/>" );
            }
            writer.write( "\n" + pad + "</services>" );
        }
    }
    
    private void writeTypeContext( Writer writer, ContextDescriptor context, String pad ) throws IOException
    {
        EntryDescriptor[] entries = context.getEntryDescriptors();
        if( entries.length > 0 )
        {
            writer.write( "\n" + pad + "<context>" );
            for( int i=0; i<entries.length; i++ )
            {
                EntryDescriptor entry = entries[i];
                writer.write( "\n" + pad + "  <entry" );
                writer.write( " key=\"" + entry.getKey() );
                writer.write( "\" class=\"" + entry.getClassname() );
                if( entry.isOptional() )
                {
                    writer.write( "\" optional=\"true\"" );
                }
                else
                {
                    writer.write( "\" optional=\"false\"" );
                }
                if( entry.isVolatile() )
                {
                    writer.write( "\" volatile=\"true\"" );
                }
                writer.write( "/>" );
            }
            writer.write( "\n" + pad + "</context>" );
        }
        else
        {
            return;
        }
    }
    
    private void writeTypeCategories( 
      Writer writer, CategoryDescriptor[] categories, String pad ) throws IOException
    {
        if( categories.length > 0 )
        {
            writer.write( "\n" + pad + "<categories>" );
            for( int i=0; i<categories.length; i++ )
            {
                CategoryDescriptor category = categories[i];
                writer.write( "\n" + pad + "  <category" );
                writer.write( " name=\"" + category.getName() );
                writer.write( 
                  "\" priority=\"" 
                  + category.getDefaultPriority().getName().toLowerCase() );
                writer.write( "\"" );

                if( category.getAttributeNames().length > 0 )
                {
                    writer.write( ">" );
                    writeAttributes( writer, category, pad + "    " );
                    writer.write( "\n" + pad + "  </category>" );
                }
                else
                {
                    writer.write( "/>" );
                }
            }
            writer.write( "\n" + pad + "</categories>" );
        }
        else
        {
            return;
        }
    }
    
    private void writeTypeState( Writer writer, State state, String pad ) throws IOException
    {
        STATE_GRAPH_ENCODER.writeState( writer, state, pad );
        writer.write( "\n" );
    }
    
    private void writeTypeParts( Writer writer, PartReference[] parts, String pad ) throws IOException
    {
        COMPONENT_ENCODER.writeParts( writer, parts, pad );
    }
    
    private void writeAttributes( Writer writer, Descriptor descriptor, String pad ) throws IOException
    {
        String[] names = descriptor.getAttributeNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = descriptor.getAttribute( name, "" );
            writer.write( "\n" + pad + "<property" );
            writer.write( " name=\"" + name );
            writer.write( "\" value=\"" + value );
            writer.write( "\"/>" );
        }
    }
}

