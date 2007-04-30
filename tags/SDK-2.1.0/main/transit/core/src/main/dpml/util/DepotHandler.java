/* 
 * Copyright 2005-2006 Stephen McConnell.
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

package dpml.util;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Logging message handler that redirects log messages from a subprocess to 
 * a remote logging system.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DepotHandler extends Handler
{
    private static final PID ID = new PID();
    
    private final Socket m_socket;
    private final OutputStream m_output;
    private final ObjectOutputStream m_stream;

   /**
    * Creation of a new handler instance.
    * @exception Exception if an error occurs
    */
    public DepotHandler() throws Exception
    {
        this( "localhost", 2020 );
    }
    
   /**
    * Creation of a new handler instance.
    * @param host the log server host
    * @param port the port
    * @exception IOException if an IO error occurs
    */
    public DepotHandler( String host, int port ) throws IOException
    {
        m_socket = new Socket( host, port );
        m_output = m_socket.getOutputStream();
        m_stream = new ObjectOutputStream( new BufferedOutputStream( m_output ) );
    }

   /**
    * Flush the handler.
    */
    public void flush()
    {
    }

   /**
    * Close the log record handler.
    */
    public void close()
    {
        try
        {
            m_output.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        try
        {
            m_socket.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

   /**
    * Publish a log record. The implementation packages the supplied log
    * record under a LogStatement, binding the record with the associated process
    * identifier.  The statement is subsequently writen to a socket established 
    * within the instance constructor.
    *
    * @param record the log record to publish
    */
    public void publish( LogRecord record )
    {
        synchronized( m_socket )
        {
            if( m_socket.isClosed() )
            {
                return;
            }

            try
            {
                LogStatement statement = new LogStatement( ID, record );
                m_stream.writeObject( statement );
                m_stream.flush();
            }
            catch( SocketException e )
            {
                e.printStackTrace();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}

