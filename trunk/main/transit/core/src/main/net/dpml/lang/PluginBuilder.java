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
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.transit.Transit;
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
public class PluginBuilder
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

    private static final ErrorHandler ERROR_HANDLER =
        new InternalErrorHandler();

   /**
    * Construct a plugin from a supplied uri.
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
        builder.setErrorHandler( ERROR_HANDLER );
        
        final Document document = builder.parse( input );
        final DocumentType docType = document.getDoctype();
        final Element root = document.getDocumentElement();
        String namespace = root.getNamespaceURI();
        final PluginHelper helper = getPluginHelper( namespace );
        URI uri = new URI( url.toString() );
        return helper.resolve( uri, root );
    }
    
    private PluginHelper getPluginHelper( String namespace ) throws Exception
    {
        if( null == namespace )
        {
            return new DefaultPluginHelper();
        }
        else
        {
            URI uri = new URI( namespace );
            ClassLoader classloader = Plugin.class.getClassLoader();
            Object[] args = new Object[0];
            Object instance = 
              Transit.getInstance().getRepository().getPlugin( classloader, uri, args );
            if( instance instanceof PluginHelper )
            {
                return (PluginHelper) instance;
            }
            else
            {
                final String error = 
                  "Namespace artifact argument [" 
                  + namespace
                  + "] assigned as the plugin helper established an instance of ["
                  + instance.getClass().getName()
                  + "] which is not assignable to the " 
                  + PluginHelper.class.getName()
                  + " interface.";
                throw new IllegalArgumentException( error );
            }
        }
    }
    
    private static final class InternalErrorHandler implements ErrorHandler
    {
        public void error( SAXParseException e ) throws SAXException
        {
            System.out.println( "ERROR: " + e.getMessage() );
        }
        public void fatalError( SAXParseException e ) throws SAXException
        {
            System.out.println( "FATAL: " + e.getMessage() );
        }
        public void warning( SAXParseException e ) throws SAXException
        {
            System.out.println( "WARN: " + e.getMessage() );
        }
    }
}
