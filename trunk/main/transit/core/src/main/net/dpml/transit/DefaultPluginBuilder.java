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

package net.dpml.transit;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.transit.util.ElementHelper;

import net.dpml.lang.Plugin;
import net.dpml.lang.Strategy;
import net.dpml.lang.Classpath;
import net.dpml.lang.SaxMonitor;
import net.dpml.lang.DTD;
import net.dpml.lang.DTDResolver;
import net.dpml.lang.PluginFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility class supporting the reading of standard plugins definitions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class PluginBuilder
{
    private static final DTD[] DTDS = new DTD[]
    {
        new DTD( 
          DefaultPlugin.STANDARD_PUBLIC_ID, 
          DefaultPlugin.STANDARD_SYSTEM_ID, 
          DefaultPlugin.STANDARD_RESOURCE, null )
    };
    
    private static final DTDResolver DTD_RESOLVER =
        new DTDResolver( DTDS, PluginBuilder.class.getClassLoader() );
        
        
    private Logger m_logger;
    
    public PluginBuilder( Logger logger )
    {
        m_logger = logger;
    }
    
   /**
    * Construct a plugin from a supplied uri.  Plugin construction will be 
    * delegated top a plugin helper identified by the xml ns urn of the root
    * document (note - this is subject to review).
    *
    * @param url the plugin url
    * @return the plugin
    * @exception Exception if an error occurs during plugin loading
    */
    public Plugin load( final URL url ) throws Exception
    {
        URLConnection connection = url.openConnection();
        InputStream input = connection.getInputStream();
        
        final DocumentBuilderFactory factory =
          DocumentBuilderFactory.newInstance();
        factory.setValidating( true );
        factory.setNamespaceAware( true );
        factory.setExpandEntityReferences( true );
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver( DTD_RESOLVER );
        ErrorHandler errors = new SaxMonitor( m_logger );
        builder.setErrorHandler( errors );
        
        final Document document = builder.parse( input );
        final DocumentType docType = document.getDoctype();
        final Element root = document.getDocumentElement();
        
        String spec = root.getNamespaceURI(); // <------------ review !
        
        String title = getTitle( root );
        String description = getDescription( root );
        URI uri = new URI( url.toString() );
        Strategy strategy = getStrategy( root );
        Classpath classpath = getClasspath( root );
        
        if( null == spec )
        {
            return new DefaultPlugin( title, description, uri, strategy, classpath );
        }
        else
        {
            PluginFactory pluginFactory = getPluginFactory( spec );
            return pluginFactory.newPlugin( title, description, uri, strategy, classpath ); 
        }
    }
    
    private PluginFactory getPluginFactory( String spec ) throws Exception
    {
        URI uri = new URI( spec );
        ClassLoader classloader = Plugin.class.getClassLoader();
        Object[] args = new Object[]{m_logger};
        Object instance = 
          Transit.getInstance().getRepository().getPlugin( classloader, uri, args );
        if( instance instanceof PluginFactory )
        {
            return (PluginFactory) instance;
        }
        else
        {
            final String error = 
              "Plugin factory artifact argument [" 
              + spec
              + "] established an instance of ["
              + instance.getClass().getName()
              + "] which is not assignable to " 
              + PluginFactory.class.getName()
              + ".";
            throw new IllegalArgumentException( error );
        }
    }
    
    protected String getTitle( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            Element elem = ElementHelper.getChild( element, "title" );
            if( null == elem )
            {
                return null;
            }
            else
            {
                return ElementHelper.getValue( element );
            }
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
            Element elem = ElementHelper.getChild( element, "description" );
            if( null == elem )
            {
                return null;
            }
            else
            {
                return ElementHelper.getValue( element );
            }
        }
    }
    
    protected Strategy getStrategy( Element root )
    {
        Element strategy = ElementHelper.getChild( root, "strategy" );
        if( null == strategy )
        {
            final String error = 
              "Required strategy element is not present in plugin descriptor.";
            throw new IllegalStateException( error );
        }
        
        String classname = ElementHelper.getAttribute( strategy, "class", StandardHandler.class.getName() );
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
    
    protected Classpath getClasspath( Element root ) throws Exception
    {
        Element classpath = ElementHelper.getChild( root, "classpath" );
        if( null == classpath )
        {
            final String error = 
              "Required classpath element is not present in plugin descriptor.";
            throw new IllegalStateException( error );
        }
        
        URI[] sys = buildURIs( classpath, "system" );
        URI[] pub = buildURIs( classpath, "public" );
        URI[] prot = buildURIs( classpath, "protected" );
        URI[] priv = buildURIs( classpath, "private" );
        return new DefaultClasspath( sys, pub, prot, priv );
    }
    
    public Plugin resolve( 
      String title, String description, URI uri, Strategy strategy, Classpath classpath ) throws Exception
    {
        return new DefaultPlugin( title, description, uri, strategy, classpath );
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
}
