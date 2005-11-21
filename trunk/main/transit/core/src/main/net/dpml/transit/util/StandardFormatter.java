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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardFormatter extends Formatter 
{
    private static final PID ID = new PID();

    private static final String SEPARATOR = System.getProperty( "line.separator" );

    /**
     * Format a LogRecord using a style appropriate for console messages. The
     * the log record message will be checked for a process identifier that is 
     * prepended to the raw message using the convention "$[" + PID + "] ".  If
     * a process id is resolved the value is assigned to the process block and 
     * the raw message is trimmed.  The resulting formatted message is presented 
     * in the form "[PID  ] [LEVEL  ] (log.category): the message".
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format( LogRecord record ) 
    {
        String process = getProcessHeader( record );
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
        String message = getFormattedMessage( record );
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

    private String getFormattedMessage( LogRecord record )
    {
        String message = formatMessage( record );
        if( message.startsWith( "$[" ) )
        {
            int n = message.indexOf( "] " );
            return message.substring( n + 2 );
        }
        else
        {
            return message;
        }
    }

    private String getLogHeader( LogRecord record )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        buffer.append( record.getLevel().getLocalizedName() );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, LEVEL_HEADER_WEIDTH ) + "] ";
    }

    private String getProcessHeader( LogRecord record )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        String message = record.getMessage();
        if( message.startsWith( "$[" ) )
        {
            int n = message.indexOf( "] " );
            String id = message.substring( 2, n );
            buffer.append( id );
        }
        else
        {
            buffer.append( ID.getValue() );
        }
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, PROCESS_HEADER_WIDTH ) + "] ";
    }

    private static final int LEVEL_HEADER_WEIDTH = 8;
    private static final int PROCESS_HEADER_WIDTH = 6;
}
