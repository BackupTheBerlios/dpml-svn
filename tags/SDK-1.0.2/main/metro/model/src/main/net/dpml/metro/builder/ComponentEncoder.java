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
import java.net.URI;

import javax.xml.XMLConstants;

import net.dpml.component.ActivationPolicy;
import net.dpml.component.Directive;

import net.dpml.lang.Value;
import net.dpml.lang.ValueEncoder;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.LookupDirective;
import net.dpml.metro.data.ValueDirective;

import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Priority;

import net.dpml.util.Encoder;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentEncoder extends ComponentConstants implements Encoder
{
    private static final String XML_HEADER = "<?xml version=\"1.0\"?>";
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
    
    private static final ValueEncoder VALUE_ENCODER = new ValueEncoder();
    
   /** 
    * Export a component directive to an output stream as XML.
    * @param directive the component directive
    * @param output the output stream
    * @exception IOException if an IO error occurs
    */
    public void export( ComponentDirective directive, OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        
        writer.write( XML_HEADER );
        writer.write( "\n\n" );
        writer.write( PARTIAL_COMPONENT_HEADER );
        writeAttributes( writer, directive, "" );
        writeBody( writer, directive, "  " );
        writer.write( "\n" );
        writer.write( "</component>" );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }
    
   /** 
    * Export a component directive to an output stream as XML.
    * @param writer the print writer
    * @param object the object to encode
    * @param pad character offset
    * @exception IOException if an IO error occurs
    */
    public void encode( Writer writer, Object object, String pad ) throws IOException
    {
        if( object instanceof ComponentDirective )
        {
            writeTaggedComponent( writer, (ComponentDirective) object, null, pad, true );
        }
        else
        {
            final String error = 
              "Encoding subject is not recognized."
              + "\nClass: " + object.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

   /** 
    * Export a component directive to an output stream as XML.
    * @param writer the print writer
    * @param directive the component directive
    * @param pad character offset
    * @exception IOException if an IO error occurs
    */
    public void writeComponent( 
      Writer writer, ComponentDirective directive, String pad ) throws IOException
    {
        writeTaggedComponent( writer, directive, null, pad );
    }
    
   /** 
    * Export a tagged component directive to an output stream as XML.
    * @param writer the print writer
    * @param directive the component directive
    * @param key the key identifying the component
    * @param pad character offset
    * @exception IOException if an IO error occurs
    */
    public void writeTaggedComponent( 
      Writer writer, ComponentDirective directive, String key, String pad ) throws IOException
    {
        writeTaggedComponent( writer, directive, key, pad, true );
    }
    
   /** 
    * Export a tagged component directive to an output stream as XML.
    * @param writer the print writer
    * @param directive the component directive
    * @param key the key identifying the component
    * @param pad character offset
    * @param flag true if the xml namespace should be included
    * @exception IOException if an IO error occurs
    */
    public void writeTaggedComponent( 
      Writer writer, ComponentDirective directive, String key, String pad, boolean flag ) throws IOException
    {
        writer.write( "\n" + pad + "<component" );
        if( flag )
        {
            writer.write( " xmlns=\"" + COMPONENT_SCHEMA_URN + "\"" );
        }
        if( null != key )
        {
            writer.write( " key=\"" + key + "\"" );
        }
        writer.write( "\n" + pad + "   " );
        writeAttributes( writer, directive, pad + "   " );
        writeBody( writer, directive, pad + "  " );
        writer.write( "\n" + pad + "</component>" );
    }
    
    void writeAttributes( 
      Writer writer, ComponentDirective directive, String pad ) throws IOException
    {
        String classname = directive.getClassname();
        if( null != classname )
        {
            writer.write( " type=\"" + classname + "\"" );
        }
        URI uri = directive.getBaseURI();
        if( null != uri )
        {
            writer.write( " uri=\"" + uri.toASCIIString() + "\"" );
        }
        String name = directive.getName();
        if( null != name )
        {
            writer.write( "\n" + pad + " name=\"" + name + "\"" );
        }
        LifestylePolicy lifestyle = directive.getLifestylePolicy();
        if( null != lifestyle )
        {
            writer.write( "\n" + pad + " lifestyle=\"" + lifestyle.getName() + "\"" );
        }
        CollectionPolicy collection = directive.getCollectionPolicy();
        if( null != collection )
        {
            writer.write( "\n" + pad + " collection=\"" + collection.getName() + "\"" );
        }
        ActivationPolicy activation = directive.getActivationPolicy();
        if( null != activation )
        {
            writer.write( "\n" + pad + " activation=\"" + activation.getName() + "\"" );
        }
        writer.write( ">" );
    }
    
    void writeBody( 
      Writer writer, ComponentDirective directive, String pad ) throws IOException
    {
        CategoriesDirective categories = directive.getCategoriesDirective();
        ContextDirective context = directive.getContextDirective();
        PartReference[] parts = directive.getPartReferences();
        writeCategoriesDirective( writer, categories, pad );
        writeContextDirective( writer, context, pad );
        writeParts( writer, parts, pad, false );
    }
    
    private void writeCategoriesDirective( 
      Writer writer, CategoriesDirective categories, String pad ) throws IOException
    {
        if( null == categories )
        {
            return;
        }
        
        String name = categories.getName();
        Priority priority = categories.getPriority();
        String target = categories.getTarget();
        CategoryDirective[] subCategories = categories.getCategories();
        
        if( isaNullValue( name ) && isaNullPriority( priority ) && isaNullValue( target ) 
          &&  ( subCategories.length == 0 ) )
        {
            return;
        }
        
        writer.write( "\n" + pad + "<categories" );
        if( !isaNullValue( name ) )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( !isaNullPriority( priority ) )
        {
            writer.write( " priority=\"" + priority.getName() + "\"" );
        }
        if( !isaNullValue( target ) )
        {
            writer.write( " target=\"" + target + "\"" );
        }
        if( subCategories.length == 0 )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            for( int i=0; i<subCategories.length; i++ )
            {
                CategoryDirective directive = subCategories[i];
                if( directive instanceof CategoriesDirective )
                {
                    CategoriesDirective c = (CategoriesDirective) directive;
                    writeCategoriesDirective( writer, c, pad + "  " );
                }
                else
                {
                    writeCategoryDirective( writer, directive, pad + "  " );
                }
            }
            writer.write( "\n" + pad + "</categories>" );
        }
    }
    
    private boolean isaNullPriority( Priority priority )
    {
        if( null == priority )
        {
            return true;
        }
        else
        {
            return Priority.DEBUG.equals( priority );
        }
    }
    
    private boolean isaNullValue( String value )
    {
        if( null == value )
        {
            return true;
        }
        else
        {
            return "".equals( value );
        }
    }
    
    private void writeCategoryDirective( 
      Writer writer, CategoryDirective category, String pad ) throws IOException
    {
        String name = category.getName();
        Priority priority = category.getPriority();
        String target = category.getTarget();
        
        writer.write( "\n" + pad + "<category" );
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( null != priority )
        {
            writer.write( " priority=\"" + priority.getName() + "\"" );
        }
        if( null != target )
        {
            writer.write( " target=\"" + target + "\"" );
        }
        writer.write( "/>" );
    }
    
    private void writeContextDirective( 
      Writer writer, ContextDirective context, String pad ) throws IOException
    {
        if( null == context )
        {
            return;
        }
        
        String classname = context.getClassname();
        PartReference[] parts = context.getDirectives();
        
        if( ( null == classname ) && ( parts.length == 0 ) )
        {
            return;
        }
        
        writer.write( "\n" + pad + "<context" );
        if( null != classname )
        {
            writer.write( " class=\"" + classname + "\"" );
        }
        if( parts.length == 0 )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            writeContextEntries( writer, parts, pad + "  " );
            writer.write( "\n" + pad + "</context>" );
        }
    }
    
   /**
    * Write a collection of part references.
    * @param writer the writer
    * @param parts the part refernece array
    * @param pad the offset
    * @param flag true if the xml namespace should be included
    * @exception IOException if an IO error occurs
    */
    protected void writeParts( 
      Writer writer, PartReference[] parts, String pad, boolean flag ) throws IOException
    {
        if( null == parts )
        {
            return;
        }
        
        if( parts.length == 0 )
        {
            return;
        }
        else
        {
            writer.write( "\n" + pad + "<parts>" );
            writePartReferences( writer, parts, pad + "  ", flag );
            writer.write( "\n" + pad + "</parts>" );
        }
    }
    
    private void writePartReferences(
      Writer writer, PartReference[] parts, String pad, boolean flag ) throws IOException
    {
        for( int i=0; i<parts.length; i++ )
        {
            PartReference ref = parts[i];
            writePartReference( writer, ref, pad, flag );
        }
    }
    
    private void writeContextEntries(
      Writer writer, PartReference[] parts, String pad ) throws IOException
    {
        for( int i=0; i<parts.length; i++ )
        {
            PartReference ref = parts[i];
            writeContextEntry( writer, ref, pad );
        }
    }
    
    private void writeContextEntry(
      Writer writer, PartReference part, String pad ) throws IOException
    {
        String key = part.getKey();
        if( null == key )
        {
            throw new IllegalStateException( "key" );
        }
        Directive directive = part.getDirective();
        if( null == directive )
        {
            throw new IllegalStateException( "directive" );
        }
        if( directive instanceof ValueDirective )
        {
            ValueDirective value = (ValueDirective) directive;
            writeEntry( writer, key, value, pad );
        }
        else if( directive instanceof LookupDirective )
        {
            LookupDirective value = (LookupDirective) directive;
            writeLookupEntry( writer, key, value, pad );
        }
        else
        {
            String classname = directive.getClass().getName();
            final String message = "WARNING: UNRECOGNIZED ENTRY: "+ classname;
            System.out.println( "# " + message );
            System.out.println( "# key: " + key );
            System.out.println( "# class: " + classname );
            writer.write( "\n" + pad + "<!-- " + message + " -->" );
            writer.write( "\n" + pad + "<!-- " );
            writer.write( "\n" + pad + "key: " + key );
            writer.write( "\n" + pad + "class: " + directive.getClass().getName() );
            writer.write( "\n" + pad + "-->" );
        }
    }
    
    private void writeLookupEntry(
      Writer writer, String key, LookupDirective directive, String pad ) throws IOException
    {
        String classname = directive.getServiceClassname();
        writer.write( "\n" + pad + "<entry key=\"" + key + "\" lookup=\"" + classname + "\"/>" );
    }
    
    private void writePartReference(
      Writer writer, PartReference part, String pad, boolean flag ) throws IOException
    {
        String key = part.getKey();
        if( null == key )
        {
            throw new IllegalStateException( "key" );
        }
        
        Directive directive = part.getDirective();
        if( null == directive )
        {
            throw new IllegalStateException( "directive" );
        }
        if( directive instanceof ComponentDirective )
        {
            ComponentDirective component = (ComponentDirective) directive;
            writeTaggedComponent( writer, component, key, pad, flag );
        }
        else
        {
            String classname = directive.getClass().getName();
            final String error = 
              "Part reference directive class not recognized."
              + "\nClass: " + classname;
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Write a context entry.
    * @param writer the writer
    * @param key the entry key
    * @param value the value directive
    * @param pad the offset
    * @exception IOException if an IO error occurs
    */
    protected void writeEntry( Writer writer, String key, ValueDirective value, String pad ) throws IOException
    {
        String target = value.getTargetExpression();
        String method = value.getMethodName();
        
        writer.write( "\n" + pad + "<entry key=\"" + key + "\"" );
        if( null != target )
        {
            writer.write( " class=\"" + target + "\"" );
        }
        if( null != method )
        {
            writer.write( " method=\"" + method  + "\"" );
        }
        Value[] values = value.getValues();
        if( values.length > 0 )
        {
            writer.write( ">" );
            VALUE_ENCODER.encodeValues( writer, values, pad + "  " );
            writer.write( "\n" + pad + "</entry>" );
        }
        else
        {
            String v = value.getBaseValue();
            if( null != v )
            {
                writer.write( " value=\"" + v + "\"" );
            }
            writer.write( "/>" );
        }
    }    
}
