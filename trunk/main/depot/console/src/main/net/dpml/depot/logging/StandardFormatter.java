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

package net.dpml.depot.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.dpml.transit.util.ExceptionHelper;

/**
 * Logging message formatter that includes the category in the logging statement.
 */
public class StandardFormatter extends Formatter 
{
    private String lineSeparator = System.getProperty( "line.separator");

    /**
     * Format a LogRecord using the classic style.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format( LogRecord record ) 
    {
        StringBuffer buffer = new StringBuffer();
  
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
        String message = formatMessage( record );
        buffer.append( message );
        buffer.append( lineSeparator );
        if( record.getThrown() != null ) 
        {
            boolean trace = record.getLevel().intValue() > 900;
            Throwable cause = record.getThrown();
            String error = ExceptionHelper.packException( cause, trace );
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
        return tag.substring( 0, 8 ) + "] ";
    }
}
