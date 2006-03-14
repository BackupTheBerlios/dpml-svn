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

import javax.xml.XMLConstants;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.data.LookupDirective;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Priority;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;
import net.dpml.transit.util.ElementHelper;

import net.dpml.part.Strategy;
import net.dpml.part.StrategyBuilder;
import net.dpml.part.PartDirective;
import net.dpml.lang.BuilderException;
import net.dpml.part.PartBuilder;
import net.dpml.lang.Type;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.Directive;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentWriter extends PartBuilder
{
    private final String CONTROLLER_URI = "@CONTROLLER-URI@";
    private final String BUILDER_URI = "@BUILDER-URI@";
    
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
    
    public void writeComponent( 
      Writer writer, ComponentDirective directive, String pad ) throws IOException
    {
        writeTaggedComponent( writer, directive, null, pad );
    }
    
    public void writeTaggedComponent( 
      Writer writer, ComponentDirective directive, String key, String pad ) throws IOException
    {
        writer.write( "\n" + pad + "<component xmlns=\"" + COMPONENT_SCHEMA_URN + "\"" );
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
        writer.write( " class=\"" + classname + "\"" );
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
        writeParts( writer, parts, pad );
    }
    
    private void writeCategoriesDirective( 
      Writer writer, CategoriesDirective categories, String pad ) throws IOException
    {
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
        else if( "".equals( value ) )
        {
            return true;
        }
        else
        {
            return false;
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
    
    protected void writeParts( 
      Writer writer, PartReference[] parts, String pad ) throws IOException
    {
        if( parts.length == 0 )
        {
            return;
        }
        else
        {
            writer.write( "\n" + pad + "<parts>" );
            writePartReferences( writer, parts, pad + "  " );
            writer.write( "\n" + pad + "</parts>" );
        }
    }
    
    private void writePartReferences(
      Writer writer, PartReference[] parts, String pad ) throws IOException
    {
        for( int i=0; i<parts.length; i++ )
        {
            PartReference ref = parts[i];
            writePartReference( writer, ref, pad );
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
        if( directive instanceof ComponentDirective )
        {
            ComponentDirective component = (ComponentDirective) directive;
            writeTaggedComponent( writer, component, key, pad );
        }
        else
        {
            String classname = directive.getClass().getName();
            final String error = 
              "Part reference class not recognized."
              + "\nClass: " + classname;
            throw new IllegalArgumentException( error );
        }
    }

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
            writeValues( writer, values, pad + "  " );
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
