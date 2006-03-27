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

import java.io.Serializable;
import java.util.logging.LogRecord;

import net.dpml.lang.PID;

/**
 * Datastructure holding a log record and process identifier.  The datastructure
 * is used to bind a PID value that identifies a source JVM  with a log record 
 * raised by the JVM.
 *
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
    
   /**
    * Creation of a new log statement.
    * @param pid the JVM process identifier
    * @param record the log record
    */
    public LogStatement( PID pid, LogRecord record )
    {
        m_pid = pid;
        m_record = record;
    }
    
   /**
    * Get the process identifier identifying the source JVM.
    * @return the process identifier
    */
    public PID getPID()
    {
        return m_pid;
    }
        
   /**
    * Get the log record raised by the source JVM.
    * @return the log record
    */
    public LogRecord getLogRecord()
    {
        return m_record;
    }
    
   /**
    * Compare this object with a supplied object for equality.
    * This function returns true if the supplied object is a 
    * LogStatement with an equivalent process identifier and 
    * log record.
    * @param other the other object
    * @return true if the supplied object is equal to this object
    */
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
    
   /**
    * Get the hascode for this instance.
    * @return the instance hash value
    */
    public int hashCode()
    {
        int hash = m_pid.hashCode();
        hash ^= m_record.hashCode();
        return hash;
    }
}

