/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.station.builder;

import java.net.URI;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

import net.dpml.station.info.RegistryDescriptor;
import net.dpml.station.info.RegistryDescriptor.Entry;
import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.StartupPolicy;

import net.dpml.part.DOM3DocumentBuilder;

import net.dpml.lang.BuilderException;
import net.dpml.lang.Builder;

import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test example application sources.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class RegistryWriter
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String APPLICATION_SCHEMA_URN = "@APPLICATION-XSD-URI@";
    
   /**
    * Externalize the part to XML.
    * @param part the part to externalize
    * @param output the output stream 
    * @exception IOException if an IO error occurs
    */
    public void writeRegistryDescriptor( 
      RegistryDescriptor descriptor, OutputStream output, String pad ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writer.write( XML_HEADER );
        writer.write( "\n" );
        writer.write( "\n<registry xmlns=\"" + APPLICATION_SCHEMA_URN + "\">" );
        writer.write( "\n" );
        
        Entry[] entries = descriptor.getEntries();
        for( int i=0; i<entries.length; i++ )
        {
            Entry entry = entries[i];
            writeEntry( writer, entry, "  " );
        }
        writer.write( "\n" );
        writer.write( "\n</registry>" );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }

    private void writeEntry( Writer writer, Entry entry, String pad ) throws IOException
    {
        String key = entry.getKey();
        ApplicationDescriptor descriptor = entry.getApplicationDescriptor();
        String title = descriptor.getTitle();
        StartupPolicy policy = descriptor.getStartupPolicy();
        String policyValue = policy.getName();
        
        writer.write( "\n" + pad + "<app key=\"" + key + "\"" );
        if( null != title )
        {
            writer.write( " title=\"" + title + "\"" );
        }
        writer.write( " policy=\"" + policyValue + "\">" );
        writeJvm( writer, descriptor, pad + "  " );
        writeCodebase( writer, descriptor, pad + "  " );
        writer.write( "\n" + pad + "</app>" );
        writer.write( "\n" );
    }
    
    private void writeJvm( Writer writer, ApplicationDescriptor descriptor, String pad ) throws IOException
    {
        writer.write( "\n" + pad + "<jvm" );
        String base = descriptor.getBasePath();
        if( null != base )
        {
            writer.write( " basedir=\"" + base + "\">" );
        }
        else
        {
            writer.write( ">" );
        }
        int startup = descriptor.getStartupTimeout();
        int shutdown = descriptor.getShutdownTimeout();
        writer.write( "\n" + pad + "  <startup>" + startup + "</startup>" );
        writer.write( "\n" + pad + "  <shutdown>" + shutdown + "</shutdown>" );
        Properties properties = descriptor.getSystemProperties();
        String[] names = (String[]) properties.keySet().toArray( new String[0] );
        if( names.length > 0 )
        {
            writer.write( "\n" + pad + "  <properties>" );
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                String value = properties.getProperty( name );
                writer.write( "\n" + pad + "    <property name=\"" + name + "\" value=\"" + value + "\"/>" );
            }
            writer.write( "\n" + pad + "  </properties>" );
        }
        writer.write( "\n" + pad + "</jvm>" );
    }
    
    private void writeCodebase( Writer writer, ApplicationDescriptor descriptor, String pad ) throws IOException
    {
        String uri = descriptor.getCodeBaseURISpec();
        writer.write( "\n" + pad + "<codebase uri=\"" + uri + "\"" );
        ValueDirective[] values = descriptor.getValueDirectives();
        if( values.length == 0 )
        {
            writer.write( "/>" );
        }
        else
        {
            writeValueDirectives( writer, values, pad + "  " );
            writer.write( "\n" + pad + "</codebase>" );
        }
    }

    protected void writeValueDirectives( Writer writer, ValueDirective[] values, String pad ) throws IOException
    {
        for( int i=0; i<values.length; i++ )
        {
            ValueDirective value = values[i];
            writeValueDirective( writer, value, pad );
        }
    }
    
    protected void writeValueDirective( Writer writer, ValueDirective value, String pad ) throws IOException
    {
        String method = value.getMethodName();
        String target = value.getTargetExpression();
        
        writer.write( "\n" + pad + "<param" );
        if( null != target )
        {
            writer.write( " class=\"" + target + "\"" );
        }
        if( null != method )
        {
            writer.write( " method=\"" + method  + "\"" );
        }            
        if( value.isCompound() )
        {
            writer.write( ">" );
            ValueDirective[] values = value.getValueDirectives();
            writeValueDirectives( writer, values, pad + "  " );
            writer.write( "\n" + pad + "</param>" );
        }
        else
        {
            String v = value.getBaseValue();
            if( null != value )
            {
                writer.write( " value=\"" + v  + "\"" );
            }
            writer.write( "/>" );
        }
    }
}
