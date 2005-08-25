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

package net.dpml.station.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.dpml.depot.LoggingService;

import net.dpml.transit.PID;

/**
 * The LoggingServer is a remote service that handles the aggregation of 
 * log records from multiple jvm processes.
 */
public class LoggingServer extends UnicastRemoteObject implements LoggingService
{
    private final Logger m_logger = Logger.getLogger( "station" );

   /**
    * Creation of a new logging service instance.
    * @exception RemoteException if a remote exception occurs
    */
    public LoggingServer() throws RemoteException
    {
        super();
    }

   /**
    * Dispach a log record to the server for processing.  The implementation
    * prepends the log record message with the process id.  Subsequent processing
    * of log records is subject to the logging configuration applied to the 
    * JVM process in which the server is established.
    *
    * @param process the process id of the jvm initiating the log record
    * @param record the log record
    * @exception RemoteException is a remote exception occurs
    */
    public void log( PID process, LogRecord record ) throws RemoteException
    {
        String raw = record.getMessage();
        int id = process.getValue();
        String message = "$[" + id + "] " + raw;
        record.setMessage( message );
        m_logger.log( record );
    }
}

