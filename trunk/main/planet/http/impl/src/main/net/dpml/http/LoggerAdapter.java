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

import net.dpml.logging.Logger;

import net.dpml.transit.util.ExceptionHelper;

/**
 * Wrapper to redirect Jetty logging to DPML logging.
 */
public class LoggerAdapter implements org.mortbay.log.Logger
{    
    private static Logger m_LOGGER;
    
    static void setRootLogger( Logger logger )
    {
        if( null == m_LOGGER )
        {
            m_LOGGER = logger;
            System.setProperty( "org.mortbay.log.class", LoggerAdapter.class.getName() );
            m_LOGGER.debug( "logging adapter established" );
        }
        else
        {
            throw new IllegalStateException( "m_LOGGER already initialized." );
        }
    }
    
    private final Logger m_logger;
    
    public LoggerAdapter()
    {
        if( null == m_LOGGER )
        {
            throw new IllegalStateException( "m_LOGGER not initialized." );
        }
        else
        {
            m_logger = m_LOGGER;
        }
    }
    
    LoggerAdapter( Logger logger )
    {
        m_logger = logger;
    }
    
    public boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }
    
    public void info( String msg, Object arg0, Object arg1 )
    {
        if( m_logger.isInfoEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.info( message );
        }
    }
    
    public void debug( String message, Throwable cause )
    {
        if( isDebugEnabled() )
        {
            if( null == cause )
            {
                m_logger.debug( message );
            }
            else
            {
                String error = ExceptionHelper.packException( message, cause, false );
                m_logger.debug( error );
            }
        }
    }
    
    public void debug( String msg, Object arg0, Object arg1 )
    {
        if( isDebugEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.debug( message );
        }
    }
    
    public void warn( String msg,Object arg0, Object arg1 )
    {
        if( m_logger.isWarnEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.warn( message );
        }
    }
    
    public void warn( String message, Throwable error )
    {
        if( m_logger.isWarnEnabled() )
        {
            m_logger.warn( message, error );
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
        Logger logger = m_LOGGER.getChildLogger( category );
        return new LoggerAdapter( logger );
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
        return "net.dpml.logging.Logger";
    }

}

