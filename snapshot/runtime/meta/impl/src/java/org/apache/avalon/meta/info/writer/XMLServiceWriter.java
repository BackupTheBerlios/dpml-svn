/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.meta.info.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.avalon.meta.info.Descriptor;
import org.apache.avalon.meta.info.Service;

/**
 * Write {@link Service} objects to a stream as xml documents.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLServiceWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLServiceWriter
    implements ServiceWriter
{

    /**
     * Write out type representation to xml.
     *
     * @param service the service descriptor instance
     * @param stream the stream to write to
     * @throws Exception if unable to write xml
     */
    public void writeService( final Service service, final OutputStream stream )
        throws Exception
    {
        final Writer writer = new OutputStreamWriter( stream );
        writeHeader( writer );
        writeDoctype( writer, "service" );
        writer.write( "\n\n<service>" );

        writer.write( "\n  <version>");
        writer.write( service.getVersion().toString() );
        writer.write( "</version>");

        writeAttributes( writer, service );
        writer.write( "\n</service>" );
        writer.flush();
    }

   /**
    * Write the XML header.
    * @param writer the writer
     * @throws IOException if unable to write xml
    */
    private void writeHeader( final Writer writer )
        throws IOException
    {
        writer.write( "<?xml version=\"1.0\" ?>" );
    }

    /**
     * Write out DOCType delcaration.
     *
     * @param writer the writer
     * @param root the root name of document
     * @throws IOException if unable to write xml
     */
    private void writeDoctype( final Writer writer,
                               final String root )
        throws IOException
    {
        final String doctype =
            "\n<!DOCTYPE " + root
            + " PUBLIC \"-//AVALON/Service DTD Version 1.0//EN\" "
            + "\"http://avalon.apache.org/dtds/meta/service_1_0.dtd\" >";
        writer.write( doctype );
    }

    /**
     * Write out xml representation of a set of attributes.
     *
     * @param writer the writer
     * @param descriptor a meta info descriptor holding attributes
     * @throws IOException if unable to write xml
     */
    private void writeAttributes( final Writer writer, final Descriptor descriptor )
        throws IOException
    {
        String[] names = descriptor.getAttributeNames();
        if( names.length > 0 )
        {
            writer.write( "\n  <attributes>" );
            for( int i = 0; i < names.length; i++ )
            {
                writeAttribute( writer, names[i], descriptor.getAttribute( names[i] ) );
            }
            writer.write( "\n  </attributes>" );
        }
    }

    /**
     * Write out xml representation of a single attribute entry.
     *
     * @param writer the writer
     * @param key the attribute key
     * @param value the attribute value
     * @throws IOException if unable to write xml
     */
    private void writeAttribute( final Writer writer,
                                 final String key, final String value )
        throws IOException
    {
        writer.write( "\n    <attribute key=\"" );
        writer.write( key );
        writer.write( "\" value=\"" );
        writer.write( value );
        writer.write( "\"/>" );
    }
}
