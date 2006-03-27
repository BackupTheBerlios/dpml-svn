/* 
 * Copyright 2006 Stephen McConnell.
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
import java.rmi.RemoteException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import net.dpml.lang.PID;
import net.dpml.lang.LoggingService;

/**
 * Datastructure holding a log record and process identifier.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LogStatement implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final PID m_pid;
    private final LogRecord m_record;
    
    public LogStatement( PID pid, LogRecord record )
    {
        m_pid = pid;
        m_record = record;
    }
    
    public PID getPID()
    {
        return m_pid;
    }
        
    public LogRecord getLogRecord()
    {
        return m_record;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof LogStatement )
        {
            LogStatement statement = (LogStatement) other;
            if( !m_pid.equals( statement.m_pid ) )
            {
                return false;
            }
            else
            {
                return m_record.equals( statement.m_record );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = m_pid.hashCode();
        hash ^= m_record.hashCode();
        return hash;
    }
}

