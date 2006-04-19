/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.util;

/**
 * Transit Logger is a interface through which different logging solutions
 * can be provided.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Logger
{
   /**
    * Return TRUE is trace level logging is enabled.
    * @return the enabled state of trace logging
    */
    boolean isTraceEnabled();

   /**
    * Return TRUE is debug level logging is enabled.
    * @return the enabled state of debug logging
    */
    boolean isDebugEnabled();

   /**
    * Return TRUE is info level logging is enabled.
    * @return the enabled state of info logging
    */
    boolean isInfoEnabled();

   /**
    * Return TRUE is warn level logging is enabled.
    * @return the enabled state of warn level logging
    */
    boolean isWarnEnabled();

   /**
    * Return TRUE is error level logging is enabled.
    * @return the enabled state of error logging
    */
    boolean isErrorEnabled();

   /**
    * Record a trace level message.
    * @param message the trace message to record
    */
    void trace( String message );

   /**
    * Record a debug level message.
    * @param message the debug message to record
    */
    void debug( String message );

   /**
    * Record a informative message.
    * @param message the info message to record
    */
    void info( String message );

   /**
    * Record a warning message.
    * @param message the warning message to record
    */
    void warn( String message );

   /**
    * Record a warning message.
    * @param message the warning message to record
    * @param cause the causal exception
    */
    void warn( String message, Throwable cause );

   /**
    * Record a error level message.
    * @param message the error message to record
    */
    void error( String message );

   /**
    * Record a error level message.
    * @param message the error message to record
    * @param cause the causal exception
    */
    void error( String message, Throwable cause );

   /**
    * Return a child logger relative to the current logger.
    * @param category the relative category name
    * @return the child logging channel
    */
    Logger getChildLogger( String category );
}


