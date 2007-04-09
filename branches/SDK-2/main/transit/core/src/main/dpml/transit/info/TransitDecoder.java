/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.transit.info;

import dpml.util.ElementHelper;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.transit.TransitError;

import net.dpml.util.Logger;

import org.xml.sax.ErrorHandler;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Utility class supporting the reading of Transit XML configurations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class TransitDecoder
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

    private static final String NAME = "transit";

    private static final String PUBLIC_ID = 
      "-//DPML//DTD Transit Configuration Version 2.0//EN";
      
    private static final String SYSTEM_ID = 
      "http://download.dpml.net/dtds/transit_2_0.dtd";

    private static final String RESOURCE = 
      "net/dpml/transit/transit_2_0.dtd";

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
          "-//DPML//DTD Transit Configuration Version 2.0//EN", 
          "http://download.dpml.net/dtds/transit_2_0.dtd", 
          "net/dpml/transit/transit_2_0.dtd", null ),
        new DTD( 
          "-//DPML//DTD Transit Configuration Version 1.0//EN", 
          "http://download.dpml.net/dtds/transit_1_0.dtd", 
          "net/dpml/transit/transit_1_0.dtd", null )
    };

    private Logger m_logger;
    
   /**
    * Creation of a new transit configuration builder.
    * @param logger the assigned logging channel
    */
    public TransitDecoder( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Construct a transit configuration from a supplied uri.
    * @param url the configuration url
    * @return the transit configuration
    * @exception Exception if an error occurs during configuration loading
    */
    public TransitDirective decode( final URL url ) throws IOException
    {
        try
        {
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
    
            final DocumentBuilderFactory factory =
              DocumentBuilderFactory.newInstance();
            factory.setValidating( true );
            factory.setNamespaceAware( true );
            factory.setExpandEntityReferences( true );
            DocumentBuilder builder = factory.newDocumentBuilder();
            DTDResolver resolver =
              new DTDResolver( DTDS, getClass().getClassLoader() );
            builder.setEntityResolver( resolver );
            ErrorHandler errors = new SaxMonitor( m_logger );
            builder.setErrorHandler( errors );
            
            final Document document = builder.parse( input );
            final Element root = document.getDocumentElement();
            return build( root );
        }
        catch( IOException ioe )
        {
            throw ioe;
        }
        catch( Exception e )
        {
            final String error =
              "Unexpected error while attempting to decode transit configuration: "
              + url;
            throw new TransitError( error );
        }
    }

    //-------------------------------------------------------------
    // internals supporting XML to directive transformation
    //-------------------------------------------------------------
    
    private TransitDirective build( Element root ) throws IOException
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
        Element contentElement = ElementHelper.getChild( root, "content" );
        HostDirective[] hosts = buildHosts( hostsElement );
        
        CacheDirective cache = 
          new CacheDirective( 
            cachePath, cacheLayout, localPath, localLayout, hosts );
            
        return new TransitDirective( proxy, cache );
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
}
