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

package net.dpml.transit;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.LogRecord;

import net.dpml.transit.PID;

/**
 * Interface exposed by a remote logging service to local log aggregation handlers. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface LoggingService extends Remote
{
   /**
    * Logging service key.
    */
    String LOGGING_KEY = "/dpml/logging";

   /**
    * Register a log record with the log service.
    * @param process the process identifier
    * @param record the log record
    * @exception RemoteException if a remote exception occurs
    */
    void log( PID process, LogRecord record ) throws RemoteException;
}

