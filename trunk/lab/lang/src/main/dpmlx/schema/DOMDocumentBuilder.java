/*
 * Copyright 2005 Stephen J. McConnell.
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

package dpmlx.schema;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import javax.xml.validation.Validator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.dpml.transit.Artifact;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.DefaultHandler;

import org.w3c.dom.Document;

public class DOMDocumentBuilder
{
    private static final String FEATURE_SECURE_PROCESSING = 
      "http://javax.xml.XMLConstants/feature/secure-processing";
    
    private static final String W3C_XML_SCHEMA_NS_URI = 
      "http://www.w3.org/2001/XMLSchema";
      
    //
    // implementation
    //
    
    public Document parse( String source ) throws Exception
    {
        DocumentBuilder db = getDocumentBuilder();
        return db.parse( source );
    }
    
    public Document parse( URI uri ) throws Exception
    {
        DocumentBuilder db = getDocumentBuilder();
        return db.parse( uri.toASCIIString() );
    }
    
    private DocumentBuilder getDocumentBuilder() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        setupBuilderForSchemaValidation( factory );
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler( new ParserAPIUsage() );
        return builder;
    }
    
    private void setupBuilderForSchemaValidation( DocumentBuilderFactory dbf )
    {
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( W3C_XML_SCHEMA_NS_URI );
            Schema schema = schemaFactory.newSchema();
            dbf.setSchema( schema );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while configuring XML parser for schema validation.";
            throw new RuntimeException( error, e );
        }
    }
    
    private URL getURL( URI uri ) throws Exception
    {
        if( Artifact.isRecognized( uri ) )
        {
            return Artifact.createArtifact( uri ).toURL();
        }
        else
        {
            return uri.toURL();
        }
    }
}
