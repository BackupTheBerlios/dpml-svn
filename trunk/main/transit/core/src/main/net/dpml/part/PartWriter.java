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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.xml.XMLConstants;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.artifact.ArtifactNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

/**
 * Construct a part.
 */
public class PartWriter extends ValueBuilder
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String PART_SCHEMA_URN = "@PART-XSD-URI@";
    
    private static final String PART_HEADER = 
      "<part xmlns=\"" 
      + PART_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\">";

    private static final String PART_FOOTER = "</part>";

    public PartWriter( Map map )
    {
        super( map );
    }
    
   /**
    * Externalize the part to XML.
    * @param part the part to externalize
    * @param output the output stream 
    * @exception IOException if an IO error occurs
    */
    public void writePart( Part part, OutputStream output, String pad ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writer.write( XML_HEADER );
        writer.write( "\n\n" );
        writer.write( PART_HEADER );
        writer.write( "\n" );
        writeInfo( writer, part.getInfo() );
        writer.write( "\n" );
        writeStrategy( writer, part.getStrategy(), "  " );
        writer.write( "\n" );
        writeClasspath( writer, part.getClasspath() );
        writer.write( "\n\n" );
        writer.write( PART_FOOTER );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }
    
    private void writeInfo( Writer writer, Info info ) throws IOException
    {
        String title = info.getTitle();
        String description = info.getDescription();
        if( null == description )
        {
            writer.write( "\n  <info title=\"" + title + "\"/>" );
        }
        else
        {
            writer.write( "\n  <info title=\"" + title + "\">" );
            writer.write( "\n    <description>" + description + "</description>" );
            writer.write( "\n  </info>" );
        }
    }
    
    protected void writeStrategy( Writer writer, Strategy strategy, String pad ) throws IOException
    {
        URI uri = strategy.getBuilderURI();
        try
        {
            StrategyBuilder builder = (StrategyBuilder) loadObjectFromURI( uri, StrategyBuilder.class );
            builder.writeStrategy( writer, strategy, pad );
        }
        catch( Exception e )
        {
            final String error = 
              "An error occured during strategy externalization.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }

    private void writeClasspath( Writer writer, Classpath classpath ) throws IOException
    {
        writer.write( "\n  <classpath>" );
        writeClasspathCategory( writer, classpath, Category.SYSTEM );
        writeClasspathCategory( writer, classpath, Category.PUBLIC );
        writeClasspathCategory( writer, classpath, Category.PROTECTED );
        writeClasspathCategory( writer, classpath, Category.PRIVATE );
        writer.write( "\n  </classpath>" );
    }

    private void writeClasspathCategory( 
      Writer writer, Classpath classpath, Category category ) throws IOException
    {
        URI[] uris = classpath.getDependencies( category );
        if( uris.length > 0 )
        {
            String name = category.getName();
            writer.write( "\n    <" + name + ">" );
            for( int i=0; i<uris.length; i++ )
            {
                URI uri = uris[i];
                writer.write( "\n      <uri>" + uri.toASCIIString() + "</uri>" );
            }
            writer.write( "\n    </" + name + ">" );
        }
    }
}
