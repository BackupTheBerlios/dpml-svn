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

import net.dpml.lang.PID;

/**
 * Generic adapter that redirects monitor events to a logging channel.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
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
    private static final String CATEGORY = "";

    private static final PID ID = new PID();

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
    * @param logger the assigned logging channel
    */
    public LoggingAdapter( Logger logger )
    {
         m_logger = logger;
    }

   /**
    * Creation of a new console adapter that is used to redirect transit events
    * the system output stream.
    * @param category the logging channel category name
    */
    public LoggingAdapter( String category )
    {
         m_logger = Logger.getLogger( category );
    }

    // ------------------------------------------------------------------------
    // Adapter
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
            return new LoggingAdapter( Logger.getLogger( path ) );
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

    /**
     * Handle download notification.
     * @param resource the resource under attention
     * @param total the estimated download size
     * @param count the progress towards expected
     */
    public void notify( URL resource, int total, int count )
    {
        String path = resource.toString();
        if( path.startsWith( "file:" ) )
        {
            return;
        }
        if( isAnt() || ( "true".equals( System.getProperty( "dpml.subprocess" ) ) ) )
        {
            if( count == 0 )
            {
                info( "downloading [" + resource + "] (" + getFranctionalValue( total ) + ")" );
            }
            return;
        }
        
        if( isInfoEnabled() )
        {
            String max = getFranctionalValue( total );
            String value = getFranctionalValue( count );
            int pad = max.length() - value.length();
            String level = getLogHeader();
            String process = getProcessHeader();
            StringBuffer buffer = new StringBuffer( process + level );
            String name = path.substring( path.lastIndexOf( '/' ) + 1 );
            buffer.append( "(" + m_logger.getName() + "): " );
            buffer.append( "retrieving: " + name + " " );
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

   /**
    * Internal utility to return the locaized logging level name.
    * @return the localized name
    */
    private String getLogHeader()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        Level level = getLoggerLevel( m_logger );
        buffer.append( level.getLocalizedName() );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, EIGHT ) + "] ";
    }

    private String getProcessHeader()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        buffer.append( ID.getValue() );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, PROCESS_HEADER_WIDTH ) + "] ";
    }


    private Level getLoggerLevel( Logger logger )
    {
        Level level = logger.getLevel();
        if( level != null )
        {
            return level;
        }
        else
        {
            Logger parent = logger.getParent();
            if( null != parent )
            {
                return getLoggerLevel( parent );
            }
            else
            {
                final String error = 
                  "Logging level is not defined.";
                throw new IllegalStateException( error );
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
        return null != System.getProperty( "ant.home" );
        /*
        try
        {
            ClassLoader.getSystemClassLoader().loadClass( "org.apache.tools.ant.launch.AntMain" );
            return true;
        }
        catch( Exception e )
        {
            return false;
        }
        */
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
        m_logger.fine( message );
    }

    private static final int EIGHT = 8;
    private static final int PROCESS_HEADER_WIDTH = 6;
}

