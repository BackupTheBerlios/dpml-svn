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

package net.dpml.depot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import net.dpml.lang.PID;
import net.dpml.lang.LoggingService;

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
    //private final ObjectOutputStream m_stream;

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
    * @exception Exception if an error occurs
    */
    public DepotHandler( String host, int port ) throws Exception
    {
        m_socket = new Socket( host, port );
    }

   /**
    * Flush the handler.
    */
    public void flush()
    {
        //try
        //{
        //    m_stream.flush();
        //}
        //catch( Exception e )
        //{
        //    e.printStackTrace();
        //}
    }

   /**
    * Close the log record handler.
    */
    public void close()
    {
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
    * Publish a log record.
    * @param record the log record
    */
    public synchronized void publish( LogRecord record )
    {
        if( m_socket.isClosed() )
        {
            return;
        }
        try
        {
            OutputStream output = m_socket.getOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream( output );
            LogStatement statement = new LogStatement( ID, record );
            stream.writeObject( statement );
            stream.flush();
        }
        catch( SocketException e )
        {
            //System.out.println( e.toString() );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

