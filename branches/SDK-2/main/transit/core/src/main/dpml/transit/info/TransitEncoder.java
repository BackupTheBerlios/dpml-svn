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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.util.Logger;

import org.xml.sax.ErrorHandler;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Utility class supporting the writing of Transit directives to XML.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class TransitEncoder
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
    
    private Logger m_logger;
    
   /**
    * Creation of a new transit configuration builder.
    * @param logger the assigned logging channel
    */
    public TransitEncoder( Logger logger )
    {
        m_logger = logger;
    }

    //-------------------------------------------------------------
    // impl 
    //-------------------------------------------------------------
    
   /**
    * Write a transit directive to an output stream as XML.
    * @param directive the directive to externalize
    * @param output the output stream to write to
    * @exception IOException if an I/O error occurs
    */
    public void encode( TransitDirective directive, OutputStream output ) throws IOException 
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
        
        writer.write( 
          "\n    <host id=\"" + id 
          + "\" priority=\"" + priority 
          + "\" url=\"" + url 
          + "\"" );
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
}
