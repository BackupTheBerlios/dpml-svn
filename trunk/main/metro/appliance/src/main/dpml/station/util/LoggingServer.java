/* 
 * Copyright 2005-2007 Stephen McConnell.
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

package dpml.station.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import dpml.util.LogStatement;

import dpml.util.PID;

/**
 * The LoggingServer is a remote service that handles the aggregation of 
 * log records from multiple jvm processes.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LoggingServer extends Thread
{
    private static final PID PROCESS = new PID();
    private static final String LOG_SERVER_PORT_KEY = "dpml.station.logging.port";
    private static final int DEFAULT_LOG_SERVER_PORT = 2020;
    
    private static LoggingServer m_SERVER;
    
   /**
    * Log server initialization.
    */
    public static void init()
    {
        if( null == m_SERVER )
        {
            try
            {
                m_SERVER = new LoggingServer();
                m_SERVER.setDaemon( true );
                m_SERVER.setName( "DPML Logging Server" );
                m_SERVER.start();
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
        }
    }
    
    private static int getLogServerPort()
    {
        String port = System.getProperty( LOG_SERVER_PORT_KEY, null );
        if( null == port )
        {
            return DEFAULT_LOG_SERVER_PORT;
        }
        else
        {
            return Integer.parseInt( port );
        }
    }
    
    private final ServerSocket m_server;
    
    private int m_count = 0;
    
   /**
    * Creation of a new logging service instance.
    * @exception IOException if an IO exception occurs
    */
    public LoggingServer() throws IOException
    {
        this( getLogServerPort() );
    }
    
   /**
    * Creation of a new logging service instance.
    * @param port the log server port
    * @exception IOException if an IO exception occurs
    */
    public LoggingServer( int port ) throws IOException
    {
        m_server = new ServerSocket( port );
    }
    
   /**
    * Runnable implementation.
    */ 
    public void run()
    {
        while( true )
        {
            try
            {
                Socket socket = m_server.accept();
                RequestHandler handler = new RequestHandler( socket );
                Thread thread = new Thread( handler );
                thread.start();
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                m_count++;
            }
        }
    }
    
    int getErrorCount()
    {
        return m_count;
    }
    
   /**
    * Internal utility class to handle a client connection.
    */
    final class RequestHandler implements Runnable
    {
        private final Socket m_socket;
        
       /**
        * Creation of a new request handler.
        * @param socket the socket
        */
        private RequestHandler( Socket socket )
        {
            m_socket = socket;
        }
        
       /**
        * Run the process.
        */
        public void run()
        {
            try
            {
                InputStream input = m_socket.getInputStream();
                BufferedInputStream buffer = new BufferedInputStream( input );
                ObjectInputStream ois = new ObjectInputStream( buffer );
                while( true )
                {
                    Object object = ois.readObject();
                    if( object instanceof LogStatement )
                    {
                        LogStatement statement = (LogStatement) object;
                        PID pid = statement.getPID();
                        if( !PROCESS.equals( pid ) )
                        {
                            int id = pid.getValue();
                            LogRecord record = statement.getLogRecord();
                            String raw = record.getMessage();
                            String message = "$[" + id + "] " + raw;
                            record.setMessage( message );
                            Logger logger = getNamedLogger( record );
                            logger.log( record );
                        }
                    }
                }
            }
            catch( ClassNotFoundException e )
            {
                e.printStackTrace();
            }
            catch( SocketException ioe )
            {
                // ignore
            }
            catch( IOException ioe )
            {
                //ioe.printStackTrace();
            }
        }

        private Logger getNamedLogger( LogRecord record )
        {
            String name = record.getLoggerName();
            if( null != name )
            {
                return Logger.getLogger( name );
            }
            else
            {
                return Logger.getAnonymousLogger();
            }
        }
    }
}

