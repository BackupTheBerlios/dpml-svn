/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.transit.tools;

import java.net.URL;

import net.dpml.transit.monitor.Adapter;
import net.dpml.util.ExceptionHelper;
import net.dpml.util.Logger;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

/**
 * A ant montor for download messages.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AntAdapter implements Adapter
{
   /**
    * The ant task that this adapter is assigned to.
    */
    private Task m_task;

   /**
    * Creation of a new ant logging adapter.
    * @param task the task establishing the adapter
    */
    public AntAdapter( Task task )
    {
         m_task = task;
    }

   /**
    * Return TRUE is debug level logging is enabled.
    * @return the enabled state of debug logging
    */
    public boolean isDebugEnabled()
    {
        return true;
    }

   /**
    * Return TRUE is info level logging is enabled.
    * @return the enabled state of info logging
    */
    public boolean isInfoEnabled()
    {
        return true;
    }

   /**
    * Return TRUE is warn level logging is enabled.
    * @return the enabled state of warn logging
    */
    public boolean isWarnEnabled()
    {
        return true;
    }

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    public boolean isErrorEnabled()
    {
        return true;
    }

   /**
    * Record a debug level message.
    * @param message the debug message to record
    */
    public void debug( String message )
    {
        m_task.log( message, Project.MSG_VERBOSE );
    }

   /**
    * Record a informative message.
    * @param message the info message to record
    */
    public void info( String message )
    {
        m_task.log( message, Project.MSG_INFO );
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    */
    public void warn( String message )
    {
        m_task.log( message, Project.MSG_WARN );
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    * @param cause the causal exception
    */
    public void warn( String message, Throwable cause )
    {
        m_task.log( message, Project.MSG_WARN );
        if( null != cause  )
        {
            final String error = ExceptionHelper.packException( message, cause, true );
            m_task.log( error, Project.MSG_WARN );
        }
    }

   /**
    * Record a error level message.
    * @param message the error message to record
    */
    public void error( String message )
    {
        error( message, null );
    }

   /**
    * Record a error level message.
    * @param message the error message to record
    * @param e the error
    */
    public void error( String message, Throwable e )
    {
        m_task.log( message, Project.MSG_ERR );
        if( null != e )
        {
            final String error = ExceptionHelper.packException( message, e, true );
            m_task.log( error, Project.MSG_ERR );
        }
    }

   /**
    * Create a new subsidiary logging channel.
    * @param category the channel name relative to this logging channel
    * @return the new logging channel
    */
    public Logger getChildLogger( String category )
    {
        return this;
    }

    /**
     * Hnadle download notification.
     * @param resource the resource under attention
     * @param total the estimated download size
     * @param count the progress towards expected
     */
    public void notify( URL resource, int total, int count )
    {
    }
}

