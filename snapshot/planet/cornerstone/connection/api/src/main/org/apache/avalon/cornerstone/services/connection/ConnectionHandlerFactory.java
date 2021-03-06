/* 
 * Copyright 1999-2004 The Apache Software Foundation
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

package org.apache.avalon.cornerstone.services.connection;

/**
 * This interface is the way in which handlers are created.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface ConnectionHandlerFactory
{
    /**
     * Construct an appropriate ConnectionHandler.
     *
     * @return the new ConnectionHandler
     * @exception Exception if an error occurs
     */
    ConnectionHandler createConnectionHandler()
        throws Exception;

    /**
     * Release a previously created ConnectionHandler.
     * e.g. for spooling.
     */
    void releaseConnectionHandler( ConnectionHandler connectionHandler );

}

