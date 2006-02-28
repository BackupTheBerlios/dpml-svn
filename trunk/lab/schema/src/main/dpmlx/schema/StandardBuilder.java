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

//import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
//import javax.xml.validation.Schema;
//import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import org.w3c.dom.Document;

public class StandardBuilder extends DefaultHandler
{
    static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    protected static final String DEFAULT_API_TO_USE = "sax";
    
    protected static final boolean DEFAULT_XINCLUDE = false;
    
    protected static final boolean DEFAULT_SECURE_PROCESSING = false;
    
    private static final String FEATURE_SECURE_PROCESSING = 
      "http://javax.xml.XMLConstants/feature/secure-processing";
    
    private static final String W3C_XML_SCHEMA_NS_URI = 
      "http://www.w3.org/2001/XMLSchema";
    
    //
    // Constructors
    //
    
    public StandardBuilder( String[] args ) throws Exception
    {
        if (args.length == 1) 
        {
            try 
            {
                String source = args[0];
                parse( source );
            }
            catch (Exception e) 
            { 
                e.printStackTrace();
            }
        }
    }
    
    public Document parse( String source ) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        setupBuilderForSchemaValidation( dbf );
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler( new ParserAPIUsage() );
        return db.parse( source );
    }
    
    private boolean isSchemaCapable()
    {
        try
        {
            ClassLoader classloader = StandardBuilder.class.getClassLoader();
            classloader.loadClass( "javax.xml.XMLConstants" );
            return true;
        }
        catch( ClassNotFoundException e )
        {
            return false;
        }
    }
    
    private void setupBuilderForSchemaValidation( DocumentBuilderFactory dbf ) throws Exception
    {
        if( !isSchemaCapable() )
        {
            return;
        }
        else
        {
            //dbf.setFeature( FEATURE_SECURE_PROCESSING, false );
            
            Class schemaFactoryClass = getClass().getClassLoader().loadClass( "javax.xml.validation.SchemaFactory" );
            Method newInstanceMethod = schemaFactoryClass.getMethod( "newInstance", new Class[]{ String.class } );
            Object schemaFactory = newInstanceMethod.invoke( null, new Object[]{ W3C_XML_SCHEMA_NS_URI } );
            Method newSchemaMethod = schemaFactory.getClass().getMethod( "newSchema", new Class[0] );
            Object schemaObject = newSchemaMethod.invoke( schemaFactory, new Object[0] );
            Class schemaClass = getClass().getClassLoader().loadClass( "javax.xml.validation.Schema" );
            Method setSchemaMethod = DocumentBuilderFactory.class.getMethod( "setSchema", new Class[]{ schemaClass } ); 
            setSchemaMethod.invoke( dbf, new Object[]{ schemaObject } );
            
            //SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            //Schema schema = factory.newSchema();
            //dbf.setXIncludeAware( false );
            //dbf.setSchema( schema );
        }
    }
    
   /*
    public String process( String xmlFileURL ) throws Exception
    {
        StandardHandler builder = new StandardHandler();
        XMLReader parser = XMLReaderFactory.createXMLReader( PARSER_NAME );
        parser.setContentHandler( builder );
        parser.setErrorHandler( builder );
        parser.setFeature( "http://xml.org/sax/features/validation", true );
        parser.setFeature( "http://apache.org/xml/features/validation/schema", true );
        parser.setFeature( "http://apache.org/xml/features/validation/schema-full-checking", true );
        parser.parse( xmlFileURL );
        String result = builder.getResult();
        return result; 
    }

    public class StandardHandler extends DefaultHandler
    {
        StringBuffer m_result = new StringBuffer( "" );
    
        public void warning( SAXParseException ex ) 
        {
            m_result.append( "\n[WARN] " +
              getLocationString( ex ) + ": " +
              ex.getMessage() + " " );
        }
    
        public void error( SAXParseException ex )
        {
            m_result.append("\n[ERROR] " +
              getLocationString( ex ) + ": " +
              ex.getMessage() + " " );
        }
    
        public void fatalError( SAXParseException ex ) throws SAXException 
        {
            m_result.append("\n[FATAL] "+
              getLocationString( ex ) + ": "+
              ex.getMessage() + " " );
        }
        
        public String getResult()
        {
            return m_result.toString();
        }
        
        private String getLocationString( SAXParseException ex ) 
        {
            StringBuffer str = new StringBuffer();
            String systemId = ex.getSystemId();
            if( systemId != null ) 
            {
                int index = systemId.lastIndexOf( '/' );
                if( index != -1 )
                {
                    systemId = systemId.substring( index + 1 );
                }
                str.append( systemId );
            }
            str.append( ':' );
            str.append( ex.getLineNumber() );
            str.append( ':' );
            str.append( ex.getColumnNumber() );
            return str.toString();
        }
    }
    */
}
