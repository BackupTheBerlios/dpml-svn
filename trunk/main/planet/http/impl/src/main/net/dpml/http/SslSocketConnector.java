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

import java.net.URI;

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
    
   /**
    * SSL connector context definition.
    */
    public interface Context extends ConnectorContext
    {
       /**
        * Set the cipher suites.
        * @param suites the default suites argument
        */
        String[] getCipherSuites( String[] suites );

       /**
        * Return the certificate password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getPassword( String password );
        
       /**
        * Return the keystore password.
        * @param password implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getKeyPassword( String password );
        
       /**
        * Return the keystore algorithm.
        * @param algorithm implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getAlgorithm( String algorithm );
        
       /**
        * Return the keystore protocol.
        * @param protocol implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getProtocol( String protocol );
        
       /**
        * Return the keystore location uri.
        * @param keystore implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        URI getKeystore( URI keystore );
        
       /**
        * Return the keystore type.
        * @param type implementation defined default value
        * @return the supplied value unless overriden in the deployment configuration
        */
        String getKeystoreType( String type );
        
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
    }

   /**
    * Creation of a new ssl connector.
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public SslSocketConnector( Context context ) throws Exception
    {
        super();
        
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
        
        String password = context.getPassword( null );
        if( null != password )
        {
            setPassword( password );
        }
        
        String keyPassword = context.getKeyPassword( null );
        if( null != keyPassword )
        {
            setKeyPassword( keyPassword );
        }
        
        String algorithm = context.getAlgorithm( null );
        if( null != algorithm )
        {
            setAlgorithm( algorithm );
        }
        
        String protocol = context.getProtocol( null );
        if( null != protocol )
        {
            setProtocol( protocol );
        }
        
        URI keystore = context.getKeystore( null );
        if( null != keystore )
        {
            String keystorePath = keystore.toASCIIString();
            setKeystore( keystorePath );
        }
        
        String keystoreType = context.getKeystoreType( null );
        if( null != keystoreType )
        {
            setKeystoreType( keystoreType );
        }
        
        boolean wantClientAuth = context.getWantClientAuth( false );
        setWantClientAuth( wantClientAuth );
        
        boolean needClientAuth = context.getNeedClientAuth( false );
        setNeedClientAuth( needClientAuth );
        
        String[] suites = context.getCipherSuites( (String[]) null );
        if( null != suites )
        {
            setCipherSuites( suites );
        }
    }
}
