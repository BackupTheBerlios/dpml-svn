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

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.avalon.cornerstone.services.connection.
    ConnectionHandlerFactory;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

/**
 * A simple factory for creating instances of <code>SimpleConnectionHandler</code>.
 * We're not doing anything fancy (or safe) here like pooling connections or queuing
 * if we are busy.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component version="1.0" name="connection-handler-factory" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory"
 */
public class SimpleConnectionHandlerFactory
    implements ConnectionHandlerFactory, LogEnabled {
  /**
   * Internal reference to the logging channel supplied by the container.
   */
  private Logger m_logger;

  /**
   * Constructs an instance of a <code>SimpleConnectionHandler</code> that will
   * handle an incoming web request.
   *
   * @see org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory#createConnectionHandler()
   */
  public ConnectionHandler createConnectionHandler() throws Exception {
    SimpleConnectionHandler handler = new SimpleConnectionHandler();
    handler.setLogger(m_logger);
    return handler;
  }

  /**
   * Does nothing since we aren't doing anything fancy here...
   *
   * @see org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory#releaseConnectionHandler(org.apache.avalon.cornerstone.services.connection.ConnectionHandler)
   */
  public void releaseConnectionHandler(ConnectionHandler handler) {
  }

  /**
   * Sets the container-supplied logger.
   */
  public void enableLogging(Logger logger) {
    m_logger = logger;
  }
}
