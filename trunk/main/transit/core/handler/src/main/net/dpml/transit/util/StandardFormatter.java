/* 
 * Copyright 2005 Stephen McConnell.
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

package net.dpml.transit.util;

import net.dpml.transit.PID;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Logging message formatter that includes the category in the logging statement.
 */
public class StandardFormatter extends Formatter 
{
    private static final PID ID = new PID();

    private static final String SEPARATOR = System.getProperty( "line.separator" );

    /**
     * Format a LogRecord using the classic style.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format( LogRecord record ) 
    {
        String message = formatMessage( record );
        if( message.startsWith( "[" ) )
        {
            return message + SEPARATOR;
        }

        String process = getProcessHeader();
        StringBuffer buffer = new StringBuffer( process );
        String header = getLogHeader( record );
        buffer.append( header );
        if( null != record.getLoggerName() ) 
        {
            buffer.append( "(" + record.getLoggerName() + "): " );
        }
        else
        {
            buffer.append( "() " );
        }
        buffer.append( message );
        buffer.append( SEPARATOR );
        if( record.getThrown() != null ) 
        {
            Throwable cause = record.getThrown();
            String error = ExceptionHelper.packException( cause, true );
            buffer.append( error );
        }
        return buffer.toString();
    }

    private String getLogHeader( LogRecord record )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        buffer.append( record.getLevel().getLocalizedName() );
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
        return tag.substring( 0, EIGHT ) + "] ";
    }

    private static final int EIGHT = 8;
}
