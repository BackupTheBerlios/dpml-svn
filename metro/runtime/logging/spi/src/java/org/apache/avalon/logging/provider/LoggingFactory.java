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

package org.apache.avalon.logging.provider;

import org.apache.avalon.repository.provider.Factory;


/**
 * Convinience interface to construct a logging manager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggingFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface LoggingFactory extends Factory
{
   /**
    * Return of map containing the default parameters.
    *
    * @return the default parameters 
    */
    LoggingCriteria createDefaultLoggingCriteria();

   /**
    * Create a new LoggingManager using the supplied logging criteria.
    *
    * @param criteria the logging system factory criteria
    * @exception LoggingException is a logging system creation error occurs
    */
    LoggingManager createLoggingManager( LoggingCriteria criteria ) 
      throws LoggingException;
}
