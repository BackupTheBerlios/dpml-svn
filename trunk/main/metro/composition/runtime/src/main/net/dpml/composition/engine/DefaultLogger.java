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

package net.dpml.composition.engine;

import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import net.dpml.transit.util.ExceptionHelper;

/**
 * Default logging adapter.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: LoggingAdapter.java 2684 2005-06-01 00:22:50Z mcconnell@dpml.net $
 */
class DefaultLogger implements net.dpml.logging.Logger
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
    public DefaultLogger( String category )
    {
         m_logger = Logger.getLogger( category );
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

    public net.dpml.logging.Logger getChildLogger( String category )
    {
        String path = m_logger.getName();
        if( "" != path )
        {
             path = path + "." + category;
        }
        else
        {
             path = category;
        }

        if( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        path = path.replace( '/', '.' );
        if( path.startsWith( "." ) )
        {
            path = path.substring( 1 );
        }
        return new DefaultLogger( path );
    }
}

