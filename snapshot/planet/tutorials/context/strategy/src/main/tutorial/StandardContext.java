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
package tutorial;

import java.io.File;

import org.apache.avalon.framework.context.Context;

/**
 * Simple non-standard Context interface to demonstration context
 * management at the level of different context types.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface StandardContext extends Context
{
    public static final String NAME_KEY = "urn:avalon:name";
    public static final String PARTITION_KEY = "urn:avalon:partition";
    public static final String WORKING_KEY = "urn:avalon:temp";
    public static final String HOME_KEY = "urn:avalon:home";

    /**
     * Return the name assigned to the component
     * @return the name
     */
    String getName();

    /**
     * Return the partition name assigned to the component
     * @return the partition name
     */
    String getPartitionName();

    /**
     * @return a file representing the home directory
     */
    File getHomeDirectory();

    /**
     * @return a file representing the temporary working directory
     */
    File getWorkingDirectory();

}
