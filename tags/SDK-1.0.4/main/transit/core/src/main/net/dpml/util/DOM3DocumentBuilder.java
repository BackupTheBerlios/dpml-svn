/*
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

package net.dpml.util;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileNotFoundException;

import javax.xml.XMLConstants;

import net.dpml.transit.Artifact;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSInput;

/**
 * Utility class that creates a schema validating DOM3 parser.
 */
public class DOM3DocumentBuilder
{
    private final Map m_map;
    private final Logger m_logger;
    
   /**
    * Creation of a new DOM3 document builder.
    */
    public DOM3DocumentBuilder()
    {
        this( new DefaultLogger( "dom" ) );
    }
    
   /**
    * Creation of a new DOM3 document builder.
    * @param logger the assigned logging channel
    */
    public DOM3DocumentBuilder( Logger logger )
    {
        this( logger, new Hashtable() );
    }
    
   /**
    * Creation of a new DOM3 document builder.
    * @param logger the assigned logging channel
    * @param map namespace to builder uri map
    */
    public DOM3DocumentBuilder( Logger logger, Map map )
    {
        m_map = map;
        m_logger = logger;
    }
    
   /**
    * Parse an xml schema document.
    * @param uri the document uri 
    * @return the validated document
    * @exception IOException if an IO error occurs
    */
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
            config.setParameter( "error-handler", new InternalErrorHandler( m_logger, uri ) );
            config.setParameter( "resource-resolver", new InternalResourceResolver( m_map ) );
            config.setParameter( "validate", Boolean.TRUE );

            DOMInput input = new DOMInput();
            URL url = Artifact.toURL( uri );
            InputStream stream = url.openStream();
            InputStreamReader reader = new InputStreamReader( stream );
            input.setCharacterStream( reader );

            return builder.parse( input );
        }
        catch( FileNotFoundException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "DOM3 error while attempting to parse document."
              + "\nSource: " + uri;
            //String message = ExceptionHelper.packException( error, e, true );
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
        }
    }
    
   /**
    * Write a document to an output stream.
    * @param doc the document to write
    * @param output the output stream
    * @exception Exception if an error occurs
    */
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
    
   /**
    * Utility class to handle namespace uri resolves.
    */
    private static class InternalResourceResolver implements LSResourceResolver
    {
        private final Map m_map;
        
       /**
        * Creation of a new InternalResourceResolver.
        * @param map the namespace to builder map
        */
        InternalResourceResolver( Map map )
        {
            m_map = map;
        }
        
       /**
        * Resolve an LS input. 
        * @param type the node type
        * @param namespace the node namespace
        * @param publicId the public id
        * @param systemId the system id
        * @param base the base value
        * @return the LS input instance
        */
        public LSInput resolveResource( 
          String type, String namespace, String publicId, String systemId, String base )
        {
            if( XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( type ) )
            {
                DOMInput input = new DOMInput();
                input.setPublicId( publicId );
                input.setSystemId( systemId );
                input.setBaseURI( base );
                
                if( null == namespace )
                {
                    return input;
                }
                
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
            if( null == namespace )
            {
                throw new NullPointerException( "namespace" );
            }
            String value = System.getProperty( namespace );
            if( null != value )
            {
                return new URI( value );
            }
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
    
   /**
    * Internal error handler with lots of room for improvement.
    */
    private static final class InternalErrorHandler implements DOMErrorHandler
    {
        private final URI m_uri;
        private final Logger m_logger;
        
        InternalErrorHandler( Logger logger, URI uri )
        {
            m_uri = uri;
            m_logger = logger;
        }
        
       /**
        * Handle the supplied error.
        * @param error the error
        * @return a boolean value
        */
        public boolean handleError( DOMError error )
        {
            DOMLocator locator = error.getLocation();
            int line = locator.getLineNumber();
            int column = locator.getColumnNumber();
            String uri = locator.getUri();
            if( null == uri )
            {
                uri = m_uri.toString();
            }
            String position = "[" + line + ":" + column + "]";
            String message = error.getMessage();
            if( null != message )
            {
                short severity = error.getSeverity();
                final String notice = 
                  "DOM3 Validation Error" 
                  + "\nSource: " 
                  + uri + ":" + position
                  + "\nCause: " 
                  + message;
                if( severity == DOMError.SEVERITY_ERROR )
                {
                    m_logger.error( notice );
                }
                else if( severity == DOMError.SEVERITY_WARNING )
                {
                    m_logger.warn( notice );
                }
                else
                {
                    m_logger.info( notice );
                }
            }
            return true;
        }
        
        private String getLabel( DOMError error )
        {
            short severity = error.getSeverity();
            if( severity == DOMError.SEVERITY_ERROR )
            {
                return "ERROR";
            }
            else if( severity == DOMError.SEVERITY_WARNING )
            {
                return "WARNING";
            }
            else
            {
                return "FATAL";
            }
        }
    }
}
