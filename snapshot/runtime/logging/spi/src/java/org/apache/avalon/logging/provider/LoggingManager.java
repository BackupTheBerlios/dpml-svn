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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.logging.data.CategoriesDirective;

/**
 * A <code>LoggerManager</code> that supports the management of a logging hierarchy.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggingManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface LoggingManager
{
    /**
     * Standard context key for the logging manager.
     */
     String KEY = "urn:avalon:logging.manager";

    /**
     * Add a set of category entries using the supplied categories descriptor.
     * @param descriptor a set of category descriptors to be added under the path
     */
    void addCategories( CategoriesDirective descriptor );

    /**
     * Add a set of category entries relative to the supplied base category
     * path, using the supplied descriptor as the definition of subcategories.
     * @param path the category base path
     * @param descriptor a set of category descriptors to be added under
     *   the base path
     */
    void addCategories( String path, CategoriesDirective descriptor );

    /**
     * Return the Logger for the specified category.
     * @param category the category path
     * @return the logging channel
     */
    Logger getLoggerForCategory( final String category );

}
