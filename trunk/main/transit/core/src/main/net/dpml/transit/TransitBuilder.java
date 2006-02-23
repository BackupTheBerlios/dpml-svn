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
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.info.ValueDirective;

import net.dpml.lang.DTD;
import net.dpml.lang.DTDResolver;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility class supporting the reading of Transit XML configurations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitBuilder
{
    public static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    public static final String NAME = "transit";

    public static final String PUBLIC_ID = 
      "-//DPML//DTD Transit Configuration Version 1.0//EN";
      
    public static final String SYSTEM_ID = 
      "http://www.dpml.net/dtds/transit_1_0.dtd";

    public static final String RESOURCE = 
      "net/dpml/transit/transit_1_0.dtd";


    private static final String DOCTYPE = 
      "\n<!DOCTYPE "
      + NAME
      + " PUBLIC \"" 
      + PUBLIC_ID
      + "\" \""
      + SYSTEM_ID 
      + "\" >";
    

    private static final DTD[] DTDS = new DTD[]
    {
        new DTD( 
          PUBLIC_ID, 
          SYSTEM_ID, 
          RESOURCE, null )
    };

    private static final DTDResolver DTD_RESOLVER =
        new DTDResolver( DTDS, TransitBuilder.class.getClassLoader() );

    private static final ErrorHandler ERROR_HANDLER =
        new InternalErrorHandler();

   /**
    * Construct a transit configuration from a supplied uri.
    * @param url the configuration url
    * @return the configuration
    * @exception Exception if an error occurs during configuration loading
    */
    public TransitDirective load( final URL url ) throws Exception
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
        //final DocumentType docType = document.getDoctype(); 
        // TODO check doctype name and version
        final Element root = document.getDocumentElement();
        return build( root );
    }

   /**
    * Write a transit directive to an output stream as XML.
    * @param directive the directive to externalize
    * @param output the output stream to write to
    * @exception IOException if an I/O error occurs
    */
    public void write( TransitDirective directive, OutputStream output ) throws IOException 
    {
        final Writer writer = new OutputStreamWriter( output );
        try
        {
            writer.write( XML_HEADER );
            writer.write( DOCTYPE );
            
            CacheDirective cache = directive.getCacheDirective();
            String cachePath = cache.getCache();
            String cacheLayout = cache.getCacheLayout();
            writeHeader( writer, cachePath, cacheLayout );
            
            ProxyDirective proxy = directive.getProxyDirective();
            writeProxy( writer, proxy );
            
            String localPath = cache.getLocal();
            String localLayout = cache.getLocalLayout();
            writeLocal( writer, localPath, localLayout );
            
            HostDirective[] hosts = cache.getHostDirectives();
            writeHosts( writer, hosts );
            
            writeFooter( writer );
            writer.write( "\n" );
        }
        finally
        {
            writer.flush();
            writer.close();
        }
    }
    
    //-------------------------------------------------------------
    // internals supporting XML to directive transformation
    //-------------------------------------------------------------
    
    private TransitDirective build( Element root ) throws Exception
    {
        String name = root.getTagName();
        if( !NAME.equals( name ) )
        {
            final String error = 
              "Invalid root element name ["
              + name
              + "].";
            throw new IOException( error );
        }
        
        String cachePath = ElementHelper.getAttribute( root, "cache" );
        String cacheLayout = ElementHelper.getAttribute( root, "layout" );
        
        Element localElement = ElementHelper.getChild( root, "local" );
        String localPath = ElementHelper.getAttribute( localElement, "path" );
        String localLayout = ElementHelper.getAttribute( localElement, "layout" );
        
        Element proxyElement = ElementHelper.getChild( root, "proxy" );
        ProxyDirective proxy = buildProxyDirective( proxyElement );
        
        Element hostsElement = ElementHelper.getChild( root, "hosts" );
        HostDirective[] hosts = buildHosts( hostsElement );
        
        Element layoutsElement = ElementHelper.getChild( root, "layouts" );
        LayoutDirective[] layouts = buildLayouts( layoutsElement );
        
        // handlers TBD
        
        CacheDirective cache = 
          new CacheDirective( 
            cachePath, cacheLayout, localPath, localLayout,
            CacheDirective.EMPTY_LAYOUTS, hosts, CacheDirective.EMPTY_CONTENT );
        return new TransitDirective( proxy, cache );
    }
    
    private LayoutDirective[] buildLayouts( Element element ) throws Exception
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            Element[] layoutElements = ElementHelper.getChildren( element, "layout" );
            LayoutDirective[] layouts = new LayoutDirective[ layoutElements.length ];
            for( int i=0; i<layoutElements.length; i++ )
            {
                Element elem = layoutElements[i];
                layouts[i] = buildLayout( elem );
            }
            return layouts;
        }
    }
            
    private LayoutDirective buildLayout( Element element ) throws Exception
    {
        String id = ElementHelper.getAttribute( element, "id" );
        String title = ElementHelper.getAttribute( element, "title" );
        Element codebase = ElementHelper.getChild( element, "codebase" );
        String uri = ElementHelper.getAttribute( codebase, "uri" );
        ValueDirective[] values = getValueDirectives( codebase );
        return new LayoutDirective( id, title, uri, values );
    }
    
    private ValueDirective[] getValueDirectives( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            Element[] valueElements = ElementHelper.getChildren( element, "value" );
            ValueDirective[] values = new ValueDirective[ valueElements.length ];
            for( int i=0; i<valueElements.length; i++ )
            {
                Element elem = valueElements[i];
                values[i] = buildValue( elem );
            }
            return values;
        }
    }
    
    private ValueDirective buildValue( Element element )
    {
        String target = ElementHelper.getAttribute( element, "target" );
        String method = ElementHelper.getAttribute( element, "method" );
        String value = ElementHelper.getAttribute( element, "value" );
        if( value != null )
        {
            return new ValueDirective( target, method, value );
        }
        else
        {
            ValueDirective[] values = getValueDirectives( element );
            return new ValueDirective( target, method, values );
        }
    }
    
    private ProxyDirective buildProxyDirective( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String host = ElementHelper.getAttribute( element, "host" );
            Element credentialsElement = ElementHelper.getChild( element, "credentials" );
            String username = getUsername( credentialsElement );
            char[] password = getPassword( credentialsElement );
            String[] excludes = buildProxyExcludes( element );
            return new ProxyDirective( host, excludes, username, password );
        }
    }
    
    private String[] buildProxyExcludes( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            Element[] elements = ElementHelper.getChildren( element, "exclude" );
            String[] excludes = new String[ elements.length ];
            for( int i=0; i<excludes.length; i++ )
            {
                Element elem = elements[i];
                excludes[i] = ElementHelper.getValue( elem );
            }
            return excludes;
        }
    }
    
    private HostDirective[] buildHosts( Element element )
    {
        Element[] elements = ElementHelper.getChildren( element, "host" );
        HostDirective[] hosts = new HostDirective[ elements.length ];
        for( int i=0; i<hosts.length; i++ )
        {
            Element elem = elements[i];
            String id = ElementHelper.getAttribute( elem, "id" );
            int priority = Integer.parseInt( ElementHelper.getAttribute( elem, "priority" ) );
            String url = ElementHelper.getAttribute( elem, "url" );
            String layout = ElementHelper.getAttribute( elem, "layout" );
            boolean enabled = ElementHelper.getBooleanAttribute( elem, "enabled" );
            boolean trusted = ElementHelper.getBooleanAttribute( elem, "trusted" );
            String index = ElementHelper.getAttribute( elem, "index" );
            String scheme = ElementHelper.getAttribute( elem, "scheme" );
            String prompt = ElementHelper.getAttribute( elem, "prompt" );
            Element credentialsElement = ElementHelper.getChild( elem, "credentials" );
            String username = getUsername( credentialsElement );
            char[] password = getPassword( credentialsElement );
            hosts[i] = 
              new HostDirective( 
                id, priority, url, index, username, password, enabled, trusted,
                layout, scheme, prompt );
        }
        return hosts;
    }
    
    private String getUsername( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            return ElementHelper.getAttribute( element, "username" );
        }
    }
    
    private char[] getPassword( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String password = ElementHelper.getAttribute( element, "password" );
            if( null == password )
            {
                return null;
            }
            else
            {
                return password.toCharArray();
            }
        }
    }
    
    //-------------------------------------------------------------
    // internals supporting directive to XML transformation
    //-------------------------------------------------------------
    
    private void writeHeader( Writer writer, String cache, String layout ) throws IOException
    {
        writer.write( "\n\n<" + NAME + " cache=\"" + cache + "\" layout=\"" + layout + "\">" );
    }
    
    private void writeFooter( Writer writer ) throws IOException
    {
        writer.write( "\n</" + NAME + ">" );
    }

    private void writeProxy( Writer writer, ProxyDirective proxy ) throws IOException 
    {
        if( null != proxy )
        {
            String host = proxy.getHost();
            String username = proxy.getUsername();
            String password = getPassword( proxy.getPassword() );
            String[] excludes = proxy.getExcludes();
            
            boolean credentials = ( ( null != username ) || ( null != password ) );
            
            if( excludes.length == 0 && ( !credentials ) )
            {
                writer.write( 
                  "\n  <proxy host=\"" + host + "\"/>" );
            }
            else
            {
                writer.write( "\n  <proxy host=\"" + host + "\">" );
                if( credentials )
                {
                    writer.write( "\n    <credentials" );
                    if( null != username )
                    {
                        writer.write( " username=\"" + username + "\"" );
                    }
                    if( null != password )
                    {
                        writer.write( " password=\"" + password + "\"" );
                    }
                    writer.write( "/>" );
                }
                if( excludes.length > 0 )
                {
                    writer.write( "\n    <excludes>" );
                    for( int i=0; i<excludes.length; i++ )
                    {
                        String exclude = excludes[i];
                        writer.write( "\n      <exclude>" + exclude + "</exclude>" );
                    }
                    writer.write( "\n    </excludes>" );
                }
                
                writer.write( "\n  </proxy>" );
            }
        }
    }
    
    private void writeLocal( Writer writer, String path, String layout ) throws IOException 
    {
        writer.write( "\n  <local path=\"" + path + "\" layout=\"" + layout + "\"/>" );
    }
    
    private void writeHosts( Writer writer, HostDirective[] hosts ) throws IOException 
    {
        writer.write( "\n  <hosts>" );
        for( int i=0; i<hosts.length; i++ )
        {
            HostDirective host = hosts[i];
            writeHost( writer, host );
        }
        writer.write( "\n  </hosts>" );
    }
    
    private void writeHost( Writer writer, HostDirective host ) throws IOException 
    {
        String id = host.getID();
        int priority = host.getPriority();
        String url = host.getHost();
        boolean enabled = host.getEnabled();
        boolean trusted = host.getTrusted();
        String layout = host.getLayout();
        String index = host.getIndex();
        String scheme = host.getScheme();
        String prompt = host.getPrompt();
        String username = host.getUsername();
        String password = getPassword( host.getPassword() );
        boolean credentials = ( ( null != username ) || ( null != password ) );
        
        writer.write( "\n    <host id=\"" + id + "\" priority=\"" + priority + "\" url=\"" + url + "\"" );
        if( !enabled )
        {
            writer.write( " enabled=\"false\"" );
        }
        if( trusted )
        {
            writer.write( " trusted=\"true\"" );
        }
        if( null != layout )
        {
            writer.write( " layout=\"" + layout + "\"" );
        }
        if( null != index )
        {
            writer.write( " index=\"" + index + "\"" );
        }
        if( ( null != scheme ) && !scheme.equals( "" ) )
        {
            writer.write( " scheme=\"" + scheme + "\"" );
        }
        if( ( null != prompt ) && !prompt.equals( "" ) )
        {
            writer.write( " prompt=\"" + prompt + "\"" );
        }
        if( credentials )
        {
            writer.write( "\n    <credentials" );
            if( null != username )
            {
                writer.write( " username=\"" + username + "\"" );
            }
            if( null != password )
            {
                writer.write( " password=\"" + password + "\"" );
            }
            writer.write( "/>" );
            writer.write( "\n    </host>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }
    
    private String getPassword( char[] password )
    {
        if( null == password )
        {
            return null;
        }
        else
        {
            return new String( password );
        }
    }
    
    //-------------------------------------------------------------
    // utilities
    //-------------------------------------------------------------
    
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
