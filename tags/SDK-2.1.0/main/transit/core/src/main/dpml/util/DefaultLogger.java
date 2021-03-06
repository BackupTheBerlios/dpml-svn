/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.util;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Generic logging channel.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultLogger implements net.dpml.util.Logger
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    private static final PID ID = new PID();
    
    private static String clean( String category )
    {
        if( null == category )
        {
            return "";
        }
        String result = category.replace( '/', '.' );
        if( result.startsWith( "." ) )
        {
            return clean( result.substring( 1 ) );
        }
        else if( result.endsWith( "." ) )
        {
            return clean( result.substring( 0, result.length() -1 ) );
        }
        else
        {
            return result;
        }
    }

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
    public DefaultLogger()
    {
        this( "" );
    }

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    * @param category the logging channel category name
    */
    public DefaultLogger( String category )
    {
        this( Logger.getLogger( clean( category ) ) );
    }
    
   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    * @param logger the assigned logging channel
    */
    public DefaultLogger( Logger logger )
    {
         m_logger = logger;
    }

    // ------------------------------------------------------------------------
    // Logger
    // ------------------------------------------------------------------------

   /**
    * Return TRUE is trace level logging is enabled.
    * @return the enabled state of trace logging
    */
    public boolean isTraceEnabled()
    {
        return m_logger.isLoggable( Level.FINER );
    }
    
   /**
    * Return TRUE is debug level logging is enabled.
    * @return the enabled state of debug logging
    */
    public boolean isDebugEnabled()
    {
        return m_logger.isLoggable( Level.FINE );
    }

   /**
    * Return TRUE is info level logging is enabled.
    * @return the enabled state of info logging
    */
    public boolean isInfoEnabled()
    {
        return m_logger.isLoggable( Level.INFO );
    }

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    public boolean isWarnEnabled()
    {
        return m_logger.isLoggable( Level.WARNING );
    }

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    public boolean isErrorEnabled()
    {
        return m_logger.isLoggable( Level.SEVERE );
    }

   /**
    * Log a debug message is trace mode is enabled.
    * @param message the message to log
    */
    public void trace( String message )
    {
        if( isTraceEnabled() )
        {
            m_logger.finer( message );
        }
    }

   /**
    * Log a debug message is debug mode is enabled.
    * @param message the message to log
    */
    public void debug( String message )
    {
        if( isDebugEnabled() )
        {
            m_logger.fine( message );
        }
    }

   /**
    * Log a info level message.
    * @param message the message to log
    */
    public void info( String message )
    {
        if( isInfoEnabled() )
        {
            m_logger.info( message );
        }
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    */
    public void warn( String message )
    {
        if( isWarnEnabled() )
        {
            m_logger.warning( message );
        }
    }

   /**
    * Record a warning message.
    * @param message the warning message to record
    * @param cause the causal exception
    */
    public void warn( String message, Throwable cause )
    {
        if( isWarnEnabled() )
        {
            m_logger.log( Level.WARNING, message, cause );
        }
    }

   /**
    * Log a error message.
    * @param message the message to log
    */
    public void error( String message )
    {
        if( isErrorEnabled() )
        {
            m_logger.log( Level.SEVERE, message );
        }
    }

   /**
    * Log a error message.
    * @param message the message to log
    * @param e the causal exception
    */
    public void error( String message, Throwable e )
    {        
        if( isErrorEnabled() )
        {
            m_logger.log( Level.SEVERE, message, e );
        }
    }

   /**
    * Return a child logger.
    * @param category the sub-category name.
    * @return the child logging channel
    */
    public net.dpml.util.Logger getChildLogger( String category )
    {
        if( ( null == category ) || "".equals( category ) )
        {
            return this;
        }
        else
        {
            String name = m_logger.getName();
            String path = trim( name + "." + category );
            return new DefaultLogger( Logger.getLogger( path ) );
        }
    }

    private String trim( String path )
    {
        if( path.startsWith( "." ) )
        {
            return trim( path.substring( 1 ) );
        }
        else if( ".".equals( path ) )
        {
            return "";
        }
        else
        {
            return path;
        }
    }
}

