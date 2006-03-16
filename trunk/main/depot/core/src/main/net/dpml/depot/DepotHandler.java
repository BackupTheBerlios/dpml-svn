/* 
 * Copyright 2005 Stephen McConnell.
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

import java.net.URL;
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
    
    private final LoggingService m_system;

   /**
    * Creation of a new handler instance.
    * @exception Exception if an error occurs during resolution of the logging service
    */
    public DepotHandler() throws Exception
    {
        m_system = getLoggingService();
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
    }

   /**
    * Publish a log record.
    * @param record the log record
    */
    public void publish( LogRecord record )
    {
        try
        {
            m_system.log( ID, record );
        }
        catch( RemoteException e )
        {
            System.err.println( e.toString() );
        }
    }

    private LoggingService getLoggingService() throws Exception
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        try
        {
            String key = "registry:" + LoggingService.LOGGING_KEY;
            String remote = System.getProperty( "net.dpml.logging.service", key );
            URL url = new URL( remote );
            return (LoggingService) url.getContent();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( classloader );
        }
    }

}

