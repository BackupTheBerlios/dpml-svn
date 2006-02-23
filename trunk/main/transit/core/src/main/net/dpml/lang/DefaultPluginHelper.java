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

package net.dpml.lang;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.transit.util.ElementHelper;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility class supporting the reading and writing of standard plugins definitions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultPluginHelper implements PluginHelper
{
   /**
    * The default runtime handler class name.
    */
    public static final String STANDARD_HANDLER = "net.dpml.transit.StandardHandler";
    
    private static final Version VERSION = new Version( 1, 0, 0 );
    
    public Plugin resolve( URI uri, Element element ) throws Exception
    {
        final Element root = element;
        String name = root.getTagName();
        if( !"plugin".equals( name ) )
        {
            final String error = 
              "Invalid root element name ["
              + name
              + "].";
            throw new IOException( error );
        }
        
        String versionSpec = ElementHelper.getAttribute( root, "version" );
        Version version = Version.getVersion( versionSpec );
        if( isRecognized( version ) )
        {
            String title = getTitle( ElementHelper.getChild( root, "title" ) );
            String description = getDescription( ElementHelper.getChild( root, "description" ) );
            Element strategyElement = ElementHelper.getChild( root, "strategy" );
            Strategy strategy = buildStrategy( strategyElement );
            Element classpathElement = ElementHelper.getChild( root, "classpath" );
            Classpath classpath = getClasspath( classpathElement );
            return new DefaultPlugin( title, description, uri, strategy, classpath );
        }
        else
        {
            final String error = 
              "Incompatible plugin version ["
              + version
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    protected Classpath getClasspath( Element element ) throws Exception
    {
        URI[] sys = buildURIs( element, "system" );
        URI[] pub = buildURIs( element, "public" );
        URI[] prot = buildURIs( element, "protected" );
        URI[] priv = buildURIs( element, "private" );
        return new DefaultClasspath( sys, pub, prot, priv );
    }
    
    protected String getTitle( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            return ElementHelper.getValue( element );
        }
    }
    
    protected String getDescription( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            return ElementHelper.getValue( element );
        }
    }
    
    protected Strategy buildStrategy( Element strategy )
    {
        if( null == strategy )
        {
            final String error = 
              "Required strategy element is not present in plugin descriptor.";
            throw new IllegalStateException( error );
        }
        
        String classname = ElementHelper.getAttribute( strategy, "class", STANDARD_HANDLER );
        Properties properties = new Properties();
        Element[] elements = ElementHelper.getChildren( strategy, "property" );
        for( int i=0; i<elements.length; i++ )
        {
            Element element = elements[i];
            String name = ElementHelper.getAttribute( element, "name" );
            String value = ElementHelper.getAttribute( element, "value" );
            properties.setProperty( name, value );
        }
        return new DefaultStrategy( classname, properties );
    }
    
    private URI[] buildURIs( Element classpath, String key ) throws Exception
    {
        Element category = ElementHelper.getChild( classpath, key );
        if( null == category )
        {
            return null;
        }
        else
        {
            Element[] children = ElementHelper.getChildren( category, "uri" );
            URI[] uris = new URI[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String value = ElementHelper.getValue( child );
                uris[i] = new URI( value );
            }
            return uris;
        }
    }
    
    private boolean isRecognized( Version version )
    {
        return VERSION.complies( version );
    }
}
