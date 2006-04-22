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

package net.dpml.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.net.URI;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import net.dpml.http.SslSocketConnector;

import junit.framework.TestCase;

/**
 * SLL Test.
 */
public class SSLTest
    extends TestCase
{

    //~ Static fields/initializers ---------------------------------------------

    /** The request. */
    private static final String REQUEST1_HEADER= 
          "POST / HTTP/1.0\n"
        + "Host: localhost\n"
        + "Content-Type: text/xml\n"
        + "Connection: close\n"
        + "Content-Length: ";
        private static final String REQUEST1_CONTENT= 
          "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
        + "<nimbus xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "        xsi:noNamespaceSchemaLocation=\"nimbus.xsd\" version=\"1.0\">\n"
        + "</nimbus>";
        private static final String REQUEST1= 
            REQUEST1_HEADER+REQUEST1_CONTENT.getBytes().length+"\n\n"+REQUEST1_CONTENT;

    /** The expected response. */
    private static final String RESPONSE1 = "HTTP/1.1 200 OK\n"
        + "Connection: close\n"
        + "Server: Jetty(6.0.x)\n"
        + "\n"
        + "Hello world\n";
    
    // Useful constants
    private static final long PAUSE = 10L;
    private static final String HOST  = "localhost";
    private static final int PORT = 0;
    
    private File m_keystore;
    private File m_truststore;
    private int m_port;
    
    public void setUp() throws Exception
    {
        // create server keystore
        
        File test = new File( System.getProperty( "project.test.dir" ) );
        String name = "test.keystore";
        m_keystore = setupKeystore( test, name );
        
        // export trust certificate for use by the client
        
        String cname = "test.cer";
        File cert = setupTrustCertificate( test, name, cname );
        
        // create clients truststore and add trusted cert
        
        String tname = "trust.keystore";
        m_truststore = setupTrustStore( test, tname, cname );
        
        String keystore = m_keystore.toString();
        String truststore = m_truststore.toString();
        
        // setup client properties
        
        System.setProperty( "javax.net.ssl.trustStore", truststore );
        System.setProperty( "javax.net.ssl.trustStorePassword", "password" );
        System.setProperty( "javax.net.ssl.keyStore", keystore );
        System.setProperty( "javax.net.ssl.keyStorePassword", "password" );
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        Thread.sleep( 1000 );
    }
    
    //~ Methods ----------------------------------------------------------------

    /**
     * Test request/response under SSL connector.
     * @throws Exception
     * @throws InterruptedException
     */
    public void testSSL() throws Exception, InterruptedException
    {
        Server server = startServer( new HelloWorldHandler() );
        
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket client = (SSLSocket) factory.createSocket( HOST, m_port );
        client.startHandshake();
        
        OutputStream os = client.getOutputStream();
        os.write( REQUEST1.getBytes() );
        os.flush();
        String response = readResponse( client );
        client.close();
        server.stop();
        assertEquals( "response", RESPONSE1, response );
    }

    private Server startServer( Handler handler ) throws Exception
    {
        Server server = new Server();
        net.dpml.http.SslSocketConnector.Context context = new LocalContext();
        SslSocketConnector connector = new SslSocketConnector( context );
        server.setConnectors(new Connector[] {connector});
        server.setHandler(handler);
        server.start();
        m_port = connector.getLocalPort();
        return server;
    }

    private static class HelloWorldHandler
        extends AbstractHandler
    {
        public boolean handle(String target, HttpServletRequest request,
                              HttpServletResponse response, int dispatch)
                       throws IOException, ServletException
        {
            response.getOutputStream().print("Hello world");
            return true;
        }
    }
    
    private static String readResponse(Socket client) throws IOException
    {
        BufferedReader br = null;
        try 
        {
            br = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
            StringBuffer sb = new StringBuffer();
            String line;
            while( ( line = br.readLine() ) != null ) 
            {
                sb.append( line );
                sb.append( '\n' );
            }
            return sb.toString();
        }
        finally
        {
            if( br != null ) 
            {
                br.close();
            }
        }
    }

    private static File setupKeystore( File test, String name ) throws Exception
    {
        File keystore = new File( test, name );
        if( !keystore.exists() )
        {
            String[] args = 
              new String[]
              {
                "keytool",
                "-genkey",
                "-alias",
                "jetty",
                "-keyalg",
                "RSA",
                "-keypass",
                "password",
                "-storepass",
                "password",
                "-dname",
                "cn=Test ou=Test o=Test c=AU",
                "-keystore",
                name
              };
            
            System.out.println( "Creating keystore" );
            Process process = Runtime.getRuntime().exec( args, new String[0], test );
            handleProcess( process );
            process.waitFor();
        }
        else
        {
            System.out.println( "Using keystore: " + keystore );
        }
        return keystore;
    }
    
    private static File setupTrustCertificate( File test, String keystore, String certNname ) throws Exception
    {
        File cert = new File( test, certNname );
        if( !cert.exists() )
        {
            String[] args = 
              new String[]
              {
                "keytool",
                "-export",
                "-rfc",
                "-alias",
                "jetty",
                "-storepass",
                "password",
                "-keystore",
                keystore,
                "-file",
                certNname
              };
            
            System.out.println( "Exporting trusted certificate" );
            Process process = Runtime.getRuntime().exec( args, new String[0], test );
            handleProcess( process );
            process.waitFor();
        }
        else
        {
            System.out.println( "Using certificate: " + cert );
        }
        return cert;
    }
    
    private static File setupTrustStore( File test, String truststore, String cert ) throws Exception
    {
        File trust = new File( test, truststore );
        if( !trust.exists() )
        {
            String[] args = 
              new String[]
              {
                "keytool",
                "-import",
                "-file",
                cert,
                "-noprompt",
                "-alias",
                "jettycert",
                "-storepass",
                "password",
                "-keypass",
                "password",
                "-keystore",
                truststore
              };
            
            System.out.println( "Creating trust keystore" );
            Process process = Runtime.getRuntime().exec( args, new String[0], test );
            handleProcess( process );
            process.waitFor();
        }
        else
        {
            System.out.println( "Using trust store: " + trust );
        }
        return trust;
    }

    private static void handleProcess( Process process )
    {
        OutputStreamReader output = new OutputStreamReader( process.getInputStream() );
        ErrorStreamReader err = new ErrorStreamReader( process.getErrorStream() );
        output.setDaemon( true );
        err.setDaemon( true );
        output.start();
        err.start();
    }

   /**
    * SSL connector context definition.
    */
    public class LocalContext implements SslSocketConnector.Context
    {
       /**
        * Return the connector host name. 
        * @param host implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getHost( String host )
        {
            return HOST;
        }
        
       /**
        * Return the connector port. 
        * @return the assigned connector port
        */
        public int getPort()
        {
            return PORT;
        }
        
       /**
        * Return the connector header buffer size.
        * @param size implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getHeaderBufferSize( int size )
        {
            return size;
        }
        
       /**
        * Return the maximum idle time in milliseconds.
        * @param time implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getMaxIdleTime( int time )
        {
            return time;
        }
        
       /**
        * Return the request buffer size.
        * @param size implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getRequestBufferSize( int size )
        {
            return size;
        }
        
       /**
        * Return the response buffer size.
        * @param size implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getResponseBufferSize( int size )
        {
            return size;
        }
        
       /**
        * Return the accept queue size.
        * @param size implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getAcceptQueueSize( int size )
        {
            return size;
        }
        
       /**
        * Return the number of inital acceptors.
        * @param size implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getAcceptors( int size )
        {
            return size;
        }
        
       /**
        * Return the soLingerTime parameter value.
        * @param time implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getSoLingerTime( int time )
        {
            return time;
        }
        
       /**
        * Return the confidential port.
        * @param port implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getConfidentialPort( int port )
        {
            return port;
        }
        
       /**
        * Return the confidential scheme (http or https).
        * @param scheme implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getConfidentialScheme( String scheme )
        {
            return scheme;
        }
        
       /**
        * Return the integral port.
        * @param port implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public int getIntegralPort( int port )
        {
            return port;
        }
        
       /**
        * Return the integral scheme (http or https).
        * @param scheme implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getIntegralScheme( String scheme )
        {
            return scheme;
        }
        
       /**
        * Set the cipher suites.
        * @param suites the default suites argument
        */
        public String[] getCipherSuites( String[] suites )
        {
            return suites;
        }

       /**
        * Return the certificate password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getCertificatePassword( String password )
        {
            return "password";
        }
        
       /**
        * Return the keystore password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getKeyStorePassword( String password )
        {
            return "password";
        }
        
       /**
        * Return the keystore algorithm.
        * @param algorithm implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getAlgorithm( String algorithm )
        {
            return algorithm;
        }
        
       /**
        * Return the keystore protocol.
        * @param protocol implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getProtocol( String protocol )
        {
            return protocol;
        }
        
       /**
        * Return the keystore location uri.
        * @param keystore implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public URI getKeyStore( URI keystore )
        {
            return m_keystore.toURI();
        }
        
       /**
        * Return the keystore type.
        * @param type implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getKeyStoreType( String type )
        {
            return type;
        }
        
       /**
        * Return the 'want-client-authentication' policy.
        * @param flag implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public boolean getWantClientAuth( boolean flag )
        {
            return flag;
        }
       
       /**
        * Return the 'need-client-authentication' policy.
        * @param flag implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public boolean getNeedClientAuth( boolean flag )
        {
            return flag;
        }
        
       /**
        * Return the SSL context provider.
        * @param provider implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getProvider( String provider )
        {
            return provider;
        }

       /**
        * Return the keystore algorithm.
        * @param algorithm implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getTrustAlgorithm( String algorithm )
        {
            return algorithm;
        }
        
       /**
        * Return the keystore location uri.
        * @param uri implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public URI getTrustStore( URI uri )
        {
            return m_truststore.toURI();
        }
        
       /**
        * Return the keystore type.
        * @param type implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getTrustStoreType( String type )
        {
            return type;
        }
        
       /**
        * Return the trust store password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        public String getTrustStorePassword( String password )
        {
            return "password";
        }
    }
}

