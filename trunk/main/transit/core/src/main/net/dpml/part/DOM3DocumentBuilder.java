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

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.dpml.transit.Logger;
import net.dpml.transit.Artifact;
import net.dpml.transit.util.ExceptionHelper;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSInput;

public class DOM3DocumentBuilder
{
    private final Map m_map;
    
    public DOM3DocumentBuilder()
    {
        this( new Hashtable() );
    }
    
    public DOM3DocumentBuilder( Map map )
    {
        m_map = map;
    }
    
    public Document parse( URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        try
        {
            DOMImplementationRegistry registry =
                DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = 
                (DOMImplementationLS) registry.getDOMImplementation( "LS" );
            if( null == impl )
            {
                final String error = 
                  "Unable to locate a DOM3 parser.";
                throw new IllegalStateException( error );
            }
            LSParser builder = impl.createLSParser( DOMImplementationLS.MODE_SYNCHRONOUS, null );
            DOMConfiguration config = builder.getDomConfig();
            config.setParameter( "error-handler", new InternalErrorHandler() );
            config.setParameter( "resource-resolver", new InternalResourceResolver( m_map ) );
            config.setParameter( "validate", Boolean.TRUE );

            DOMInput input = new DOMInput();
            URL url = Artifact.toURL( uri );
            InputStream stream = url.openStream();
            InputStreamReader reader = new InputStreamReader( stream );
            input.setCharacterStream( reader );

            return builder.parse( input );
        }
        catch( Throwable e )
        {
            final String error = 
              "DOM3 error while attempting to parse document."
              + "\nSource: " + uri;
            String message = ExceptionHelper.packException( error, e, true );
            throw new IOException( message );
        }
    }
    
    public void write( Document doc, OutputStream output ) throws Exception
    {
        DOMImplementationRegistry registry =
            DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = 
            (DOMImplementationLS) registry.getDOMImplementation( "LS" );
        if( null == impl )
        {
            final String error = 
              "Unable to locate a DOM3 implementation.";
            throw new IllegalStateException( error );
        }
        LSSerializer domWriter = impl.createLSSerializer();
        LSOutput lsOut = impl.createLSOutput();
        lsOut.setByteStream( output );
        domWriter.write( doc, lsOut );
    }
    
    private static class InternalResourceResolver implements LSResourceResolver
    {
        private final Map m_map;
        
        InternalResourceResolver( Map map )
        {
            m_map = map;
        }
        
        public LSInput resolveResource( String type, String namespace, String publicId, String systemId, String base )
        {
            if( XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( type ) )
            {
                DOMInput input = new DOMInput();
                input.setPublicId( publicId );
                input.setSystemId( systemId );
                input.setBaseURI( base );
                try
                {
                    URI uri = resolveURI( namespace );
                    URL url = Artifact.toURL( uri );
                    InputStream stream = url.openStream();
                    InputStreamReader reader = new InputStreamReader( stream );
                    input.setCharacterStream( reader );
                }
                catch( URISyntaxException e )
                {
                    // ignore
                }
                catch( IOException e )
                {
                    // ignore
                }
                return input;
            }
            else
            {
                return null;
            }
        }
        
        private URI resolveURI( String namespace ) throws URISyntaxException
        {
            if( m_map.containsKey( namespace ) )
            {
                return (URI) m_map.get( namespace );
            }
            else
            {
                return new URI( namespace );
            }
        }
    }
    
    

    private static final class InternalErrorHandler implements DOMErrorHandler
    {
        public boolean handleError( DOMError error )
        {
            DOMLocator locator = error.getLocation();
            int line = locator.getLineNumber();
            int column = locator.getColumnNumber();
            String uri = locator.getUri();
            if( null == uri )
            {
                uri = "";
            }
            String position = "[" + line + ":" + column + "] " + uri + ", ";
            Node node = locator.getRelatedNode();
            if( null != node )
            {
                System.out.println( "# NODE: " + node );
            }
            String message = error.getMessage();
            short severity = error.getSeverity();
            if( severity == DOMError.SEVERITY_ERROR )
            {
                System.out.println("[ERROR]: " + position + message );
                final String notice = "DOM3 Validation Error: " + position + message;
                throw new RuntimeException( notice );
            }
            else if( severity == DOMError.SEVERITY_WARNING )
            {
                System.out.println("[WARNING]: " + position + message );
            }
            else
            {
                System.out.println("[FATAL]: " + position + message );
            }
            return true;
        }
    }
}
