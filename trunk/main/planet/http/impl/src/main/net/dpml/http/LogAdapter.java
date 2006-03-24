/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper to redirect Jetty logging to standard JVM logging.
 */
public class LogAdapter implements org.mortbay.log.Logger
{    
    private final Logger m_logger;
    
    public LogAdapter()
    {
        this( Logger.getLogger( "org.mortbay" ) );
    }
    
    LogAdapter( Logger logger )
    {
        m_logger = logger;
    }
    
    public boolean isDebugEnabled()
    {
        return m_logger.isLoggable( Level.FINE );
    }
    
    public void info( String msg, Object arg0, Object arg1 )
    {
        if( m_logger.isLoggable( Level.INFO ) )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.info( message );
        }
    }
    
    public void debug( String message, Throwable cause )
    {
        if( isDebugEnabled() )
        {
            m_logger.log( Level.FINE, message, cause );
        }
    }
    
    public void debug( String msg, Object arg0, Object arg1 )
    {
        if( isDebugEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.log( Level.FINE, message );
        }
    }
    
    public void warn( String msg,Object arg0, Object arg1 )
    {
        if( m_logger.isLoggable( Level.WARNING ) )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.log( Level.WARNING, message );
        }
    }
    
    public void warn( String message, Throwable error )
    {
        if( m_logger.isLoggable( Level.WARNING ) )
        {
            m_logger.log( Level.WARNING, message, error );
        }
    }

    private String format( String msg, Object arg0, Object arg1 )
    {
        int i0 = msg.indexOf( "{}" );
        
        int i1 = 0;
        if( i0 < 0 )
        { 
            i1 = -1;
        }
        else
        {
            i1 = msg.indexOf( "{}" , i0 + 2 );
        }
        
        if( ( arg1 != null ) && ( i1 >= 0 ) )
        {
            msg = 
              msg.substring( 0, i1 ) 
              + arg1 
              + msg.substring( i1 + 2 );
        }
        if( ( arg0 != null ) && ( i0 >= 0 ) )
        {
            msg = 
              msg.substring( 0,i0 )
              + arg0
              + msg.substring( i0 + 2 );
        }
        return msg;
    }
    
    public org.mortbay.log.Logger getLogger( String category )
    {
        if( ( null == category ) || "org.mortbay".equals( category ) )
        {
            return this;
        }
        else
        {
            String name = m_logger.getName();
            String path = trim( name + "." + category );
            return new LogAdapter( Logger.getLogger( path ) );
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
    
    public String toString()
    {
        return "java.util.logging.Logger/" + m_logger.getName();
    }

}

