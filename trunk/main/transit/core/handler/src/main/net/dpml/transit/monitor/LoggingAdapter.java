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

package net.dpml.transit.monitor;

import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import net.dpml.transit.util.ExceptionHelper;

/**
 * Generic adapter that redirects monitor events to a logging channel.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class LoggingAdapter implements Adapter
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * Constant kb value.
    */
    private static final int KBYTE = 1024;

   /**
    * The logging channel.
    */
    private static final String CATEGORY = "transit";

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
    public LoggingAdapter()
    {
         this( Logger.getLogger( CATEGORY ) );
    }

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    */
    public LoggingAdapter( Logger logger )
    {
         m_logger = logger;
    }

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    */
    public LoggingAdapter( String category )
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

    public net.dpml.transit.model.Logger getChildLogger( String category )
    {
        String name = m_logger.getName();
        String path = name + "." + category;
        return new LoggingAdapter( Logger.getLogger( path ) );
    }

    /**
     * Handle download notification.
     * @param resource the resource under attention
     * @param total the estimated download size
     * @param count the progress towards expected
     */
    public void notify( URL resource, int total, int count )
    {
        if( isAnt() && total == count )
        {
            info( "downloaded: " + resource );
            return;
        }

        if( isInfoEnabled() )
        {
            String max = getFranctionalValue( total );

            String value = getFranctionalValue( count );
            int pad = max.length() - value.length();
            StringBuffer buffer = new StringBuffer( "[TRANSIT] " + "Progress: " );
            for( int i=0; i < pad; i++ )
            {
                buffer.append( " " );
            }
            buffer.append( value );
            buffer.append( "k/" );
            if( total == 0 )
            {
                buffer.append( "?" );
            }
            else
            {
                buffer.append( max );
                buffer.append( "k\r" );
            }
            if( total == count )
            {
                System.out.println( buffer.toString() );
            }
            else
            {
                System.out.print( buffer.toString() );
            }
        }
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Test is the runtime environment is ant.
    * @return true if ant is visible
    */
    private boolean isAnt()
    {
        try
        {
            ClassLoader.getSystemClassLoader().loadClass( "org.apache.tools.ant.launch.AntMain" );
            return true;
        }
        catch( Exception e )
        {
            return false;
        }
    }

   /**
    * Return a string representing the number of kilobytes relative to the supplied
    * total bytes.
    * @param total the byte value
    * @return the string to log
    */
    private static String getFranctionalValue( int total )
    {
        final int offset = 3;

        float realTotal = new Float( total ).floatValue();
        float realK = new Float( KBYTE ).floatValue();
        float r = ( realTotal / realK );

        String value = new Float( r ).toString();
        int j = value.indexOf( "." );
        if( j > -1 )
        {
             int q = value.length();
             int k = q - j;
             if( k > offset )
             {
                 return value.substring( 0, j + offset );
             }
             else
             {
                 return value;
             }
        }
        else
        {
             return value;
        }
    }

   /**
    * Internal utility to log a message to system.out.
    * @param message the message to log
    */
    private void log( String message )
    {
        debug( message);
    }
}

