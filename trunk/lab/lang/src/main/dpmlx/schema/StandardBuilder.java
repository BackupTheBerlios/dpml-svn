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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

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
      
    private static boolean VALIDATING = isSchemaCapable();
    
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
        if( VALIDATING )
        {
            setupBuilderForSchemaValidation( dbf );
        }
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler( new ParserAPIUsage() );
        return db.parse( source );
    }
    
    private void setupBuilderForSchemaValidation( DocumentBuilderFactory dbf ) throws Exception
    {
        Class schemaFactoryClass = getClass().getClassLoader().loadClass( "javax.xml.validation.SchemaFactory" );
        Class schemaClass = getClass().getClassLoader().loadClass( "javax.xml.validation.Schema" );
        
        Method newInstanceMethod = schemaFactoryClass.getMethod( "newInstance", new Class[]{ String.class } );
        Object schemaFactory = newInstanceMethod.invoke( null, new Object[]{ W3C_XML_SCHEMA_NS_URI } );
        Method newSchemaMethod = schemaFactoryClass.getMethod( "newSchema", new Class[0] );
        Object schemaObject = newSchemaMethod.invoke( schemaFactory, new Object[0] );
        Method setSchemaMethod = DocumentBuilderFactory.class.getMethod( "setSchema", new Class[]{ schemaClass } ); 
        setSchemaMethod.invoke( dbf, new Object[]{ schemaObject } );
    }
    
    private static boolean isSchemaCapable()
    {
        try
        {
            ClassLoader classloader = StandardBuilder.class.getClassLoader();
            classloader.loadClass( "javax.xml.XMLConstants" );
            System.out.println( "validating" );
            return true;
        }
        catch( ClassNotFoundException e )
        {
            System.out.println( "validation disabled" );
            return false;
        }
    }
}
