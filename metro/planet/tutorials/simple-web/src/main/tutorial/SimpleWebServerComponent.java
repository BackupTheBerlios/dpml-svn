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

import java.net.ServerSocket;

import org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory;
import org.apache.avalon.cornerstone.services.connection.ConnectionManager;
import org.apache.avalon.cornerstone.services.sockets.ServerSocketFactory;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * A tutorial that demonstrates a simple web server component for merlin that
 * uses the <code>SocketManager</code> and <code>ConnectionManager</code>
 * components in the cornerstone and excalibur component packages.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 * @avalon.component version="1.0" name="simple-web-server" lifestyle="singleton"
 */
public class SimpleWebServerComponent
    implements LogEnabled, Serviceable, Parameterizable, Executable, Disposable {
  /**
   * Internal reference to the logging channel supplied by the container.
   */
  private Logger m_logger;
  /**
   * Internal reference to the socket manager supplied by the container.
   */
  private SocketManager m_socketManager;
  /**
   * Internal reference to the connection manager supplied by the container.
   */
  private ConnectionManager m_connectionManager;
  /**
   * Internal reference to connection handler factory supplied by container
   */
  private ConnectionHandlerFactory m_connectionHandlerFactory;
  /**
   * Internal reference to the web server's listening port supplied
   * by the conponent configuration.  Defaults to 80.
   */
  private int m_port;
  /**
   * Alias for the name of our HTTP connection handler.
   */
  private final static String HTTP_LISTENER = "http-listener";

  /**
   * Supply of a logging channel by the container.
   *
   * @param logger the logging channel for this component
   */
  public void enableLogging(Logger logger) {
    m_logger = logger;
    getLogger().info("logging");
  }

  /**
   * Servicing of the component by the container during which service
   * dependencies declared under the component can be resolved using the
   * supplied service manager.
   *
   * @param manager the service manager
       * @throws ServiceException if an failure occurs while servicing the component
   *
   * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
   *
   * @avalon.dependency type="org.apache.avalon.cornerstone.services.sockets.SocketManager:1.0" key="socket-manager"
   * @avalon.dependency type="org.apache.avalon.cornerstone.services.connection.ConnectionManager:1.0" key="connection-manager"
   * @avalon.dependency type="org.apache.avalon.cornerstone.services.connection.ConnectionHandlerFactory:1.0" key="connection-handler-factory"
   */
  public void service(ServiceManager manager) throws ServiceException {
    m_socketManager = (SocketManager) manager.lookup("socket-manager");
    m_connectionManager = (ConnectionManager) manager.lookup(
        "connection-manager");
    m_connectionHandlerFactory = (ConnectionHandlerFactory) manager.lookup("connection-handler-factory");
  }

  /**
   * Parameterization of the component by the container.
   * @param parameters the component parameters
   * @throws ConfigurationException if a configuration error occurs
   */
  public void parameterize( Parameters params ) throws ParameterException 
  {
      getLogger().info("Parameterizing...");
      m_port = params.getParameterAsInteger( "port", 80 );
  }

  /**
   * Component execution trigger by the container following
   * completion of the initialization stage.
   *
   * @see org.apache.avalon.framework.activity.Executable#execute()
   */
  public void execute() throws Exception {
    // Use the Cornerstone SocketManager to give us a factory object for creating
    // a plain ole server socket
    ServerSocketFactory ssf = m_socketManager.getServerSocketFactory("plain");

    // Use that factory to create a server socket
    ServerSocket serverSocket = ssf.createServerSocket(m_port);

    // attach the server socket to our connection manager
    m_connectionManager.connect(HTTP_LISTENER, serverSocket, m_connectionHandlerFactory);

    // we've started!
    getLogger().info("Started HTTP listener socket on port {" + m_port + "}");
  }

  /**
   * Component disposal trigger by the container during which
   * the component will release consumed resources.
   *
   * @see org.apache.avalon.framework.activity.Disposable#dispose()
   */
  public void dispose() {
    try {
      // forcefully tear down all handlers...
      m_connectionManager.disconnect(HTTP_LISTENER, true);
    }
    catch (Exception e) {
      getLogger().error("Unexpected error while shutting down HTTP listener", e);
    }
    m_connectionManager = null;
    m_socketManager = null;
  }

  /**
   * Return the logging channel assigned to us by the container.
   * @return the logging channel
   */
  private Logger getLogger() {
    return m_logger;
  }
}
