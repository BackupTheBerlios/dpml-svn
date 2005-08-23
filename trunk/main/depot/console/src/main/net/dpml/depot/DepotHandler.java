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

import net.dpml.transit.PID;

/**
 */
public class DepotHandler extends Handler
{
    private final LoggingService m_system;
    private final PID ID = new PID();

    public DepotHandler() throws Exception
    {
        m_system = getLoggingService();
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

    public void flush()
    {
    }

    public void close()
    {
    }

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
}

