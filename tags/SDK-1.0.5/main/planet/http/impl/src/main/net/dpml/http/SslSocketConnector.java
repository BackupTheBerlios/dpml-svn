/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import net.dpml.transit.Artifact;

import org.mortbay.resource.Resource;
import org.mortbay.jetty.security.Password;

/**
 * SSL socket connector.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SslSocketConnector extends org.mortbay.jetty.security.SslSocketConnector
{
    private static final int HEADER_BUFFER_SIZE = 4*1024;
    private static final int REQUEST_BUFFER_SIZE = 8*1024;
    private static final int RESPONSE_BUFFER_SIZE = 32*1024;
    private static final int MAXIMUM_IDLE_TIME = 30000;
    private static final int ACCEPT_QUEUE_SIZE = 0;
    private static final int ACCEPTORS = 1;
    private static final int SO_LINGER_TIME = 1000;
    private static final int CONFIDENTIAL_PORT = 0;
    private static final int INTEGRAL_PORT = 0;
    private static final boolean ASSUME_SHORT_DISPATCH = false;
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String PROTOCOL = "TLS";
    private static final String ALGORITHM = "SunX509";
    
    private transient Context m_context;
    private transient Password m_certificatePassword;
    private transient Password m_keystorePassword;
    private transient Password m_trustPassword;
    
   /**
    * SSL connector context definition.
    */
    public interface Context extends ConnectorContext
    {
       /**
        * Set the cipher suites.
        * @param suites the default suites argument
        * @return the cipher suites
        */
        //String[] getCipherSuites( String[] suites );

       /**
        * Return the keystore password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getKeyStorePassword( String password );
        
       /**
        * Return the certificate password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getCertificatePassword( String password );
        
       /**
        * Return the keystore algorithm.
        * @param algorithm implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getSecureRandomAlgorithm( String algorithm );
        
       /**
        * Return the keystore type.
        * @param type implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getKeyStoreType( String type );
        
       /**
        * Return the SSL protocol.
        * @param protocol implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getProtocol( String protocol );
        
       /**
        * Return the keystore location uri.
        * @param keystore implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        URI getKeyStore( URI keystore );
        
       /**
        * Return the 'want-client-authentication' policy.
        * @param flag implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        boolean getWantClientAuth( boolean flag );
        
       /**
        * Return the 'need-client-authentication' policy.
        * @param flag implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        boolean getNeedClientAuth( boolean flag );
        
       /**
        * Return the SSL context provider.
        * @param provider implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getProvider( String provider );
        
        // extras
        
       /**
        * Return the keystore algorithm.
        * @param algorithm implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getTrustAlgorithm( String algorithm );
        
       /**
        * Return the keystore location uri.
        * @param uri implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        URI getTrustStore( URI uri );
        
       /**
        * Return the keystore type.
        * @param type implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getTrustStoreType( String type );
        
       /**
        * Return the trust store password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getTrustStorePassword( String password );
    }
    
   /**
    * Creation of a new ssl connector.
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public SslSocketConnector( Context context ) throws Exception
    {
        super();
        
        m_context = context;
        
        String host = context.getHost( null );
        if( null != host )
        {
            setHost( host );
        }
    
        int port = context.getPort();
        setPort( port );
        
        int headerBufferSize = context.getHeaderBufferSize( HEADER_BUFFER_SIZE );
        setHeaderBufferSize( headerBufferSize );
        
        int requestBufferSize = context.getRequestBufferSize( REQUEST_BUFFER_SIZE );
        setRequestBufferSize( requestBufferSize );
        
        int responseBufferSize = context.getResponseBufferSize( RESPONSE_BUFFER_SIZE );
        setResponseBufferSize( responseBufferSize );
        
        int maxIdle = context.getMaxIdleTime( MAXIMUM_IDLE_TIME );
        setMaxIdleTime( maxIdle );
        
        int queueSize = context.getAcceptQueueSize( ACCEPT_QUEUE_SIZE );
        setAcceptQueueSize( queueSize );
        
        int acceptCount = context.getAcceptors( ACCEPTORS );
        setAcceptors( acceptCount );
        
        int linger = context.getSoLingerTime( SO_LINGER_TIME );
        setSoLingerTime( linger );
        
        int confidentialPort = context.getConfidentialPort( CONFIDENTIAL_PORT );
        setConfidentialPort( confidentialPort );
        
        Scheme confidentialScheme = Scheme.parse( context.getConfidentialScheme( "https" ) );
        setConfidentialScheme( confidentialScheme.getName() );
        
        int integralPort = context.getIntegralPort( INTEGRAL_PORT );
        setIntegralPort( integralPort );
        
        Scheme integralScheme = Scheme.parse( context.getIntegralScheme( "https" ) );
        setIntegralScheme( integralScheme.getName() );
        
        // SslSocketConnector$Context
        
        String certificatePassword = context.getCertificatePassword( null );
        if( null != certificatePassword )
        {
            m_certificatePassword = 
              Password.getPassword( KEYPASSWORD_PROPERTY, certificatePassword, null );
            setKeyPassword( certificatePassword );
        }
        
        String keystorePassword = context.getKeyStorePassword( null );
        if( null != keystorePassword )
        {
            m_keystorePassword = Password.getPassword( PASSWORD_PROPERTY, keystorePassword, null );
            setPassword( keystorePassword );
        }
        
        String algorithm = context.getSecureRandomAlgorithm( ALGORITHM );
        setSecureRandomAlgorithm( algorithm );
        
        String protocol = context.getProtocol( PROTOCOL );
        setProtocol( protocol );
        
        URI keystore = context.getKeyStore( null );
        if( null != keystore )
        {
            String keystorePath = keystore.toASCIIString();
            setKeystore( keystorePath );
        }
        
        String provider = context.getProvider( null );
        if( null != provider )
        {
            setProvider( provider );
        }
        
        String keystoreType = context.getKeyStoreType( KEYSTORE_TYPE );
        setKeystoreType( keystoreType );
        
        boolean wantClientAuth = context.getWantClientAuth( false );
        setWantClientAuth( wantClientAuth );
        
        boolean needClientAuth = context.getNeedClientAuth( false );
        setNeedClientAuth( needClientAuth );
        
        //String[] suites = context.getCipherSuites( (String[]) null );
        //if( null != suites )
        //{
        //    setCipherSuites( suites );
        //}
    }

   /**
    * Create a new SSLServerSocketFactory.
    * @return the factory
    * @exception Exception if an error occurs during factory creation
    */
    protected SSLServerSocketFactory createFactory() 
        throws Exception
    {
        final SSLContext context = getSSLContext();
        KeyManager[] keyManagers = getKeyManagers();
        TrustManager[] trustManagers = getTrustManagers();
        SecureRandom random = new SecureRandom();
        context.init( keyManagers, trustManagers, random );
        return context.getServerSocketFactory();
    }
    
    private KeyManager[] getKeyManagers() throws Exception
    {
        final String algorithm = getSecureRandomAlgorithm();
        final KeyManagerFactory factory = KeyManagerFactory.getInstance( algorithm );
        final KeyStore store = loadKeyStore();
        final char[] password = toCharArray( m_certificatePassword );
        factory.init( store, password );
        return factory.getKeyManagers();
    }
    
    private KeyStore loadKeyStore() throws Exception
    {
        final String type = getKeystoreType();
        final KeyStore keyStore = KeyStore.getInstance( type );
        final char[] password = toCharArray( m_keystorePassword );
        final String keyStorePath = getKeystore();
        final InputStream input = Resource.newResource( keyStorePath ).getInputStream();
        keyStore.load( input, password );
        return keyStore;
    }
    
    private KeyStore loadTrustStore() throws Exception
    {
        final String type = m_context.getTrustStoreType( KEYSTORE_TYPE );
        final KeyStore store = KeyStore.getInstance( type );
        final char[] password = toCharArray( m_trustPassword );
        URI uri = m_context.getTrustStore( null );
        if( null != uri )
        {
            URL url = Artifact.toURL( uri );
            final InputStream input = Resource.newResource( url ).getInputStream();
            store.load( input, password );
            return store;
        }
        else
        {
            return null;
        }
    }
    
    private TrustManager[] getTrustManagers() throws Exception
    {
        final String algorithm = m_context.getTrustAlgorithm( ALGORITHM );
        final TrustManagerFactory factory = TrustManagerFactory.getInstance( algorithm );
        final KeyStore store = loadTrustStore();
        if( store != null )
        {
            factory.init( store );
            return factory.getTrustManagers();
        }
        else
        {
            return new TrustManager[0];
        }
    }

    private char[] toCharArray( Password value )
    {
        if( null == value )
        {
            return null;
        }
        else
        {
            return value.toString().toCharArray();
        }
    }
    
    private SSLContext getSSLContext() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        final String protocol = getProtocol();
        final String provider = getProvider();
        if( null == provider )
        {
            return SSLContext.getInstance( protocol );
        }
        else
        {
            return SSLContext.getInstance( protocol, provider );
        }
    }
}
    
