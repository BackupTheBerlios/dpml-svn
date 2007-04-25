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

package dpml.lang;

import dpml.util.DefaultLogger;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileNotFoundException;

import net.dpml.lang.DecodingException;

import net.dpml.transit.Artifact;
import net.dpml.transit.InvalidArtifactException;

import net.dpml.util.Logger;

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
import org.w3c.dom.ls.LSException;

/**
 * Utility class that creates a schema validating DOM3 parser.
 */
public class DOM3DocumentBuilder
{
    private final Logger m_logger;
    
   /**
    * Creation of a new DOM3 document builder.
    */
    public DOM3DocumentBuilder()
    {
        this( new DefaultLogger( "dpml.lang" ) );
    }
    
   /**
    * Creation of a new DOM3 document builder.
    * @param logger the assigned logging channel
    */
    public DOM3DocumentBuilder( final Logger logger )
    {
        m_logger = logger;
    }
    
   /**
    * Parse an xml schema document.
    * @param uri the document uri 
    * @return the validated document
    * @exception IOException if an IO error occurs
    */
    public Document parse( final URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        URL url = Artifact.toURL( uri );
        return parse( url );
    }
    
   /**
    * Parse an xml schema document.
    * @param url the document url 
    * @return the validated document
    * @exception IOException if an IO error occurs
    */
    public Document parse( URL url ) throws IOException
    {
        if( null == url )
        {
            throw new NullPointerException( "url" );
        }
        URLConnection connection = url.openConnection();
        return parse( connection );
    }
    
   /**
    * Parse an xml schema document.
    * @param connection the document url connection
    * @return the validated document
    * @exception IOException if an IO error occurs
    */
    public Document parse( URLConnection connection ) throws IOException
    {
        if( null == connection )
        {
            throw new NullPointerException( "connection" );
        }
        URL url = connection.getURL();
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
            
            URI uri = url.toURI();
            InternalErrorHandler errorHandler = new InternalErrorHandler( m_logger, uri );
            config.setParameter( "error-handler", errorHandler );
            config.setParameter( "resource-resolver", new InternalResourceResolver( m_logger ) );
            config.setParameter( "validate", Boolean.TRUE );
            DOMInput input = new DOMInput();
            
            InputStream stream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader( stream );
            input.setCharacterStream( reader );
            
            Document doc = builder.parse( input );
            int n = errorHandler.getErrorCount();
            if( n > 0 )
            {
                final String error = 
                  "Document validation failure."
                  + "\nURL: " + url;
                throw new DecodingException( error, null, null );
            }
            doc.setDocumentURI( uri.toASCIIString() );
            return doc;
        }
        catch( FileNotFoundException e )
        {
            throw e;
        }
        catch( InvalidArtifactException e )
        {
            throw e;
        }
        catch( DecodingException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to decode document [" + url + "]";
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
        private static final StandardNamespaceResolver RESOLVER = 
          new StandardNamespaceResolver();
          
        private final Logger m_logger;
        
       /**
        * Creation of a new InternalResourceResolver.
        * @param map the namespace to builder map
        */
        InternalResourceResolver( Logger logger )
        {
            m_logger = logger;
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
            LSResourceResolver[] resolvers = getNamespaceResolvers();
            for( LSResourceResolver resolver : resolvers )
            {
                LSInput input = resolver.resolveResource( type, namespace, publicId, systemId, base );
                if( null != input )
                {
                    return input;
                }
            }
            
            if( "link:xsd:dpml/lang/dpml-module#1.0".equals( namespace ) )
            {
                final String error = 
                  "The document namespace \"link:xsd:dpml/lang/dpml-module#1.0\" has been "
                  + "replaced by \"dpml:library\".";
                throw new LSException( LSException.PARSE_ERR, error );
            }
            else if( "link:xsd:dpml/lang/dpml-part#1.0".equals( namespace ) )
            {
                final String error = 
                  "Namespace resolution error (migration issue)."
                  + "\n\nThe document namespace \"link:xsd:dpml/lang/dpml-part#1.0\" has been "
                  + "replaced by the namespace \"dpml:part\".";
                throw new LSException( LSException.PARSE_ERR, error );
            }
            else
            {
                final String error = 
                  "Unable to resolve a schema for the namespace [" 
                  + namespace 
                  + "]";
                throw new LSException( LSException.PARSE_ERR, error );
            }
        }
        
        private LSResourceResolver[] getNamespaceResolvers()
        {
            ArrayList<LSResourceResolver> list = new ArrayList<LSResourceResolver>();
            ServiceLoader<LSResourceResolver> loaders = 
              ServiceLoader.load( LSResourceResolver.class );
            for( LSResourceResolver resolver : loaders )
            {
                list.add( resolver );
            }
            return list.toArray( new LSResourceResolver[0] );
        }
    }
    
   /**
    * Internal error handler with room for improvement.
    */
    private static final class InternalErrorHandler implements DOMErrorHandler
    {
        private final URI m_uri;
        private final Logger m_logger;
        
        private int m_count = 0;
        
        InternalErrorHandler( Logger logger, URI uri )
        {
            m_uri = uri;
            m_logger = logger;
        }
        
        int getErrorCount()
        {
            return m_count;
        }
        
       /**
        * Handle the supplied error.
        * @param error the error
        * @return a boolean value
        */
        public boolean handleError( DOMError error )
        {
            m_count++;
            DOMLocator locator = error.getLocation();
            int line = locator.getLineNumber();
            int column = locator.getColumnNumber();
            String uri = locator.getUri();
            if( null == uri )
            {
                uri = m_uri.toString();
            }
            
            String source = uri;
            if( !( ( line == -1 ) && column == -1 ) )
            {
                source = source + " [" + line + ":" + column + "]";
            }
            String message = error.getMessage();
            final String notice = 
              message
              + "\n" 
              + source;
            short severity = error.getSeverity();
            if( severity == DOMError.SEVERITY_WARNING )
            {
                m_logger.warn( notice );
                //return true;
                return false;
            }
            else
            {
                m_logger.warn( notice );
                return false;
            }
        }
    }
}
