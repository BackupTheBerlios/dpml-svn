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

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.log.format.Formatter;

/**
 * Factory for Formatters.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FormatterFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface FormatterFactory
{
    String DEFAULT_FORMAT =
       "[%7.7{priority}] (%{category}): %{message}\\n%{throwable}";

   /**
    * Creation of a new formatter using a supplied configuration.
    * @param config the formatter configuration
    * @return the formatter instance
    * @exception IllegalArgumentException if the formatter type is unknown
    */
    public Formatter createFormatter( final Configuration config )
      throws IllegalArgumentException;

   /**
    * Creation of a new formatter.
    * @param type the formatter type
    * @param pattern the formatter pattern
    * @return the formatter
    */
    Formatter createFormatter( String type, String pattern );
}
