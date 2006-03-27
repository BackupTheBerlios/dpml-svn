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

package net.dpml.station.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.util.logging.Level;

import net.dpml.lang.PID;

import net.dpml.depot.LogStatement;

import net.dpml.transit.util.ExceptionHelper;
import net.dpml.transit.util.StandardFormatter;

/**
 * The LoggingServer is a remote service that handles the aggregation of 
 * log records from multiple jvm processes.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LoggingServer implements Runnable
{
    private static final PID PID = new PID();

    private final ServerSocket m_server;
    
    private int m_count = 0;
    
   /**
    * Creation of a new logging service instance.
    * @exception RemoteException if a remote exception occurs
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
                InputStream input = socket.getInputStream();
                ObjectInputStream stream = new ObjectInputStream( input );
                Object object = stream.readObject();
                if( object instanceof LogStatement )
                {
                    LogStatement statement = (LogStatement) object;
                    PID pid = statement.getPID();
                    if( !PID.equals( pid ) )
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
                input.close();
                socket.close();
            }
            catch( Throwable e )
            {
                m_count++;
            }
        }
    }
    
    int getErrorCount()
    {
        return m_count;
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

