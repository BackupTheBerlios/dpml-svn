/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.logging.logkit;

import org.apache.avalon.framework.logger.Logger;
import org.apache.log.LogEvent;
import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.util.StackIntrospector;

import org.apache.avalon.util.exception.ExceptionHelper;

/**
 * This formatter extends ExtendedPatternFormatter so that
 * CascadingExceptions are formatted with all nested exceptions.
 *
 * <ul>
 * <li><code>class</code> : outputs the name of the class that has logged the
 *     message. The optional <code>short</code> subformat removes the
 *     package name. Warning : this pattern works only if formatting occurs in
 *     the same thread as the call to Logger, i.e. it won't work with
 *     <code>AsyncLogTarget</code>.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: StandardFormatter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class StandardFormatter
    extends ExtendedPatternFormatter
{
    private static final int TYPE_CLASS = MAX_TYPE + 1;

    private static final String TYPE_CLASS_STR = "class";
    private static final String TYPE_CLASS_SHORT_STR = "short";

    public static final boolean DEFAULT_STACKTRACE_POLICY = true;

    private final boolean m_stacktrace;

    /**
     * Construct the formatter with the specified pattern
     * and which which prints out exceptions to stackDepth of 8.
     *
     * @param pattern The pattern to use to format the log entries
     * @since 1.0
    */
    public StandardFormatter( final String pattern )
    {
        this( pattern, DEFAULT_STACKTRACE_POLICY );
    }

    /**
     * Construct the formatter with the specified pattern
     * and which which prints out exceptions to stackDepth specified.
     *
     * @param pattern The pattern to use to format the log entries
     * @param trace if TRUE generate a stack trace
     * @since 1.0
     */
    public StandardFormatter( final String pattern, final boolean trace )
    {
        super( pattern );
        m_stacktrace = trace;
    }

    /**
     * Utility method to format stack trace.
     *
     * @param throwable the throwable instance
     * @param format ancilliary format parameter - allowed to be null
     * @return the formatted string
     */
    protected String getStackTrace( final Throwable throwable, final String format )
    {
        if( null == throwable )
        {
            return "";
        }
        return ExceptionHelper.packException( throwable, m_stacktrace );
    }

    /**
     * Retrieve the type-id for a particular string.
     *
     * @param type the string
     * @return the type-id
     */
    protected int getTypeIdFor( final String type )
    {
        if( type.equalsIgnoreCase( TYPE_CLASS_STR ) )
        {
            return TYPE_CLASS;
        }
        else
        {
            return super.getTypeIdFor( type );
        }
    }

   /**
    * Return the result of formaltting a pattern run.
    * @param event the log event
    * @param run the patter formatter pattern run
    * @return the formatted string
    */
    protected String formatPatternRun( LogEvent event, PatternFormatter.PatternRun run )
    {
        switch( run.m_type )
        {
            case TYPE_CLASS:
                return getClass( run.m_format );
            default:
                return super.formatPatternRun( event, run );
        }
    }

    /**
     * Finds the class that has called Logger.
     */
    private String getClass( String format )
    {
        final Class clazz = StackIntrospector.getCallerClass( Logger.class );

        if( null == clazz )
        {
            return "Unknown-class";
        }
        else
        {
            // Found : the caller is the previous stack element
            String className = clazz.getName();

            // Handle optional format
            if( TYPE_CLASS_SHORT_STR.equalsIgnoreCase( format ) )
            {
                int pos = className.lastIndexOf( '.' );

                if( pos >= 0 )
                {
                    className = className.substring( pos + 1 );
                }
            }

            return className;
        }
    }
}
