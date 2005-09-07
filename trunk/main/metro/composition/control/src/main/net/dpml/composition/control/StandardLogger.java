/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition.control;

import java.net.URL;

import net.dpml.transit.model.Logger;

/**
 * Default logging adapter.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: LoggingAdapter.java 2684 2005-06-01 00:22:50Z mcconnell@dpml.net $
 */
public class StandardLogger implements net.dpml.logging.Logger
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Logger m_logger;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    */
    public StandardLogger( Logger logger )
    {
         m_logger = logger;
    }

    // ------------------------------------------------------------------------
    // Adapter
    // ------------------------------------------------------------------------

   /**
    * Return TRUE is debug level logging is enabled.
    * @return the enabled state of debug logging
    */
    public boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }

   /**
    * Return TRUE is info level logging is enabled.
    * @return the enabled state of info logging
    */
    public boolean isInfoEnabled()
    {
        return m_logger.isInfoEnabled();
    }

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    public boolean isWarnEnabled()
    {
        return m_logger.isWarnEnabled();
    }

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    public boolean isErrorEnabled()
    {
        return m_logger.isErrorEnabled();
    }

   /**
    * Log a debug message is debug mode is enabled.
    * @param message the message to log
    */
    public void debug( String message )
    {
        m_logger.debug( message );
    }

   /**
    * Log a info level message.
    * @param message the message to log
    */
    public void info( String message )
    {
        m_logger.info( message );
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    */
    public void warn( String message )
    {
        m_logger.warn( message );
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    */
    public void warn( String message, Throwable cause )
    {
        m_logger.warn( message, cause );
    }

   /**
    * Log a error message.
    * @param message the message to log
    */
    public void error( String message )
    {
        m_logger.error( message );
    }

   /**
    * Log a error message.
    * @param message the message to log
    * @param cause the causal exception
    */
    public void error( String message, Throwable cause )
    {        
        m_logger.error( message, cause );
    }

    public net.dpml.logging.Logger getChildLogger( String category )
    {
        if( ( null == category ) || ( "".equals( category ) ) )
        {
            return this;
        }
        else
        {
            return new StandardLogger( m_logger.getChildLogger( category ) );
        }
    }
}
