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

package org.apache.avalon.test.playground.basic;

import java.io.File;

import org.apache.avalon.framework.context.Context;

/**
 * Simple non-standard Context interface to demonstration context
 * management at the level of different context types.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface BasicContext extends Context
{
    /**
     * @return a string containing a location value
     */
    String getLocation();

    /**
     * @return a file representing the working directory
     */
    File getWorkingDirectory();

}
