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
package org.apache.avalon.db;

/**
 * Defintion of the DatabaseService service contract.
 * 
 * @avalon.service version="1.0"
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/05 15:20:02 $
 */
public interface DatabaseService {
    /**
     * Starts the database service.
     * 
     * @throws Exception if an error occurs while starting the service
     */
    void start() throws Exception;
    
    /**
     * Shuts down the database service.
     * 
     * @throws Exception if an error occurs while shutting down the service
     */
    void stop() throws Exception;
}
