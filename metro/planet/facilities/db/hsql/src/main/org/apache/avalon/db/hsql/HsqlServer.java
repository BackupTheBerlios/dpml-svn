/* 
 * Copyright 2004 Apache Software Foundation
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
package org.apache.avalon.db.hsql;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import org.apache.avalon.framework.logger.Logger;
import org.hsqldb.HsqlServerFactory;
import org.hsqldb.HsqlSocketRequestHandler;

import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

/**
 * This is a database server facility for the HypersonicSQL database.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/11 21:08:03 $
 */
public class HsqlServer {
    /**
     * TCP listening socket backlog.
     */
    private static final int BACKLOG = 50;
    /**
     * Default host name to bind the HSQL database server TCP socket
     * to.  The default value is the localhost, that is 127.0.0.1.
     */
    private static final String DEFAULT_HOST = "127.0.0.1";
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    /**
     * Default HSQL database server TCP socket listening port.
     * The default value is the HSQL standard port 9001.
     */
    private static final int DEFAULT_PORT = 9001;
    /**
     * TCP listening socket connection timeout (msecs).
     */
    private static final int TIMEOUT = 500;
    /**
     * Flag indicating if the HSQL database server will operate in
     * a debug logging mode.
     */
    private boolean m_debug;
    /**
     * Host to bind the database TCP/IP listener to. Default is localhost.
     */
    private InetAddress m_host;
    /**
     * The HSQLDB database server and socket request handler.
     */
    private HsqlSocketRequestHandler m_hsqlSocketRequestHandler;

    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    /**
     * The logging channel for this component.
     */
    private final Logger m_logger;

    //---------------------------------------------------------
    // state
    //---------------------------------------------------------
    
    /**
     * Alias of the HSQL database server.
     */
    private String m_name;
    /**
     * TCP listening port of the HSQL database server.
     */
    private int m_port;
    /**
     * Main thread managing the server connection socket.
     */
    private Thread m_serverSocketThread;
    /**
     * Flag indicating if the HSQL database server will operate in
     * a silent logging mode.
     */
    private boolean m_silent;

    /**
     * Creates a new instance of an <code>HsqlServer</code> object.
     * 
     * @param name alias of the HSQL database server instance
     * @param port listening port of the HSQL database server
     * @param host <code>InetAddress</code> to bind the HSQL database
     * server to
     * @param debug if <b>true</b>, the HSQL database server will be
     * started in debug mode
     * @param silent if <b>true</b>, the HSQL database server will
     * operate in a silent logging mode
     */
    public HsqlServer( String name,
                       int port,
                       InetAddress host,
                       boolean debug,
                       boolean silent,
                       Logger logger)
    {
        m_name = name;
        m_port = port;
        m_host = host;
        m_debug = debug;
        m_silent = silent;
        m_logger = logger;
    }

    /**
     * Starts the socket server and hands off connections to
     * the thread pool for processing by a worker thread.
     * 
     * @param serverSocket the serverSocket to manage
     */
    private void runServer( ServerSocket serverSocket ) 
    {
        if ( m_logger.isInfoEnabled() ) 
        {
            m_logger.info( "Running database connection server..." );
        }
        
        ThreadedExecutor runner = new ThreadedExecutor();
        try 
        {
            // accept connections until this thread is interrupted...
            while ( !Thread.interrupted() ) 
            {
                try
                {
                    // when a connection is made, spin off a thread to
                    // process the connection, and immediately go back to
                    // listening for other connections
                    final Socket connection = serverSocket.accept();
                    runner.execute( 
                       new Runnable() {
                          public void run() {
                             // who connected to us?
                             final String remoteHost = 
                                connection.getInetAddress().getHostName();
                             final String remoteIP = 
                                connection.getInetAddress().getHostAddress();
                            
                             // hand-off the socket connection to HSQLDB...
                             m_hsqlSocketRequestHandler.handleConnection( connection );

                             if ( m_logger.isDebugEnabled() ) {
                                final String message = 
                                  "database connection from " 
                                  + remoteHost 
                                  + " (" 
                                  + remoteIP 
                                  + ")";
                                m_logger.debug( message );
                             }
                          }
                       }
                    );
                } 
                catch ( InterruptedIOException iioe )
                {
                    // Ignore these - thrown when accept() times out
                }
                catch ( Exception e ) 
                {
                    // IOException - if an I/O error occurs when waiting 
                    // for a connection.
                    // SecurityException - if a security manager exists 
                    // and its checkListen method doesn't allow the operation.
                    if ( m_logger.isWarnEnabled() ) 
                    {
                        m_logger.warn( e.getMessage(), e );
                    }
                }
            }
        } 
        finally 
        {
            if ( runner != null ) 
            {
                runner = null;
            }
            if ( serverSocket != null ) 
            {
                try 
                {
                    serverSocket.close();
                } 
                catch ( IOException ignore ) 
                {
                    // ignore...
                }
                serverSocket = null;
            }
        }
    }

    /**
     * Starts the HSQL database server instance.
     * 
     * @throws Exception if an error occurs while starting the server
     */
    public void startServer() throws Exception
    {
        // create the HSQLDB server and socket request handler
        try 
        {
            m_hsqlSocketRequestHandler = 
              HsqlServerFactory.createHsqlServer( m_name, m_debug, m_silent );
        } 
        catch ( SQLException sqle ) 
        {
            if ( m_logger.isErrorEnabled() ) 
            {
                final String error = 
                  "Error starting HSQL database: " 
                  + sqle.getMessage();
                m_logger.error( error, sqle );
            }
            throw sqle;
        }
        
        // start up primary socket server 
        final ServerSocket serverSocket = new ServerSocket( m_port,
            BACKLOG, m_host );
        serverSocket.setSoTimeout( TIMEOUT );
        m_serverSocketThread = 
          new Thread( new Runnable() {
            public void run()
            {
                runServer( serverSocket );
            }
          } );
        m_serverSocketThread.start();
        
        if ( m_logger.isInfoEnabled() ) 
        {
            m_logger.info( "Hypersonic SQL listening on port " + m_port );
        }
    }
    
    /**
     * Shuts down the HSQL database server instance.
     * 
     * @throws Exception if an error occurs while shutting down the server
     */
    public void stopServer() throws Exception
    {
        // tell HSQLDB that all connections are being shutdown...
        if ( m_hsqlSocketRequestHandler != null ) 
        {
            m_hsqlSocketRequestHandler.signalCloseAllServerConnections();
        }
        
        // shutdown server socket...
        if ( m_serverSocketThread != null ) 
        {
            m_serverSocketThread.interrupt();
        }
    }
}
