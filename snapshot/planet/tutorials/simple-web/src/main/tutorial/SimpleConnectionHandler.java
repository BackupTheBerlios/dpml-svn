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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.avalon.cornerstone.services.connection.ConnectionHandler;
import org.apache.avalon.framework.logger.Logger;

/**
 * Simple connection handler for our web server.  It conforms to HTTP/1.0 only.
 * This handler will perform the following tasks:
 * <ul>
 *  <li>Parse the request
 *  <li>Build the reply
 *  <li>Send the reply
 * </ul>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class SimpleConnectionHandler
    implements ConnectionHandler {
  /**
   * Internal reference to the logging channel supplied by the container.
   */
  private Logger m_logger;

  /**
   * Processes connections as they occur.
   *
   * @param socket the socket connection
   * @throws IOException if an error reading from the socket occurs
   * @throws ProtocolException if an error handling the connection occurs
   *
   * @see org.apache.avalon.cornerstone.services.connection.ConnectionHandler#handleConnection(java.net.Socket)
   */
  public void handleConnection(Socket socket) throws IOException,
      ProtocolException {
    Request req;

    // who has connected to us?
    String remoteHost = socket.getInetAddress().getHostName();
    String remoteIP = socket.getInetAddress().getHostAddress();
    getLogger().info("Connection received on port " + socket.getLocalPort()
                     + " from " + remoteHost + " (" + remoteIP + ")");

    // get a request object...
    InputStream input = socket.getInputStream();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] bytes = new byte[1024];
    int count = input.read(bytes);
    while (count > -1) {
      output.write(bytes, 0, count);
      if (input.available() == 0) {
        break;
      }
      count = input.read(bytes);
    }
    output.flush();
    output.close();

    getLogger().debug("Preparing byte buffer...");
    ByteBuffer bb = ByteBuffer.allocate(output.size());
    bb.put(output.toByteArray());
    bb.flip();
    getLogger().debug("Byte buffer prepared");
    try {
      getLogger().debug("Parsing request...");
      req = Request.parse(bb);
    }
    catch (MalformedRequestException e) {
      getLogger().error(e.getMessage(), e);
      throw new ProtocolException(e.getMessage());
    }

    // Handle the request -- only accept HTTP GET
    if (!req.isGet()) {
      getLogger().debug("Sending HTTP 405...");
      Reply.sendHttpReply(socket.getOutputStream(), Reply.HTTP_405,
                          "This server only accepts GET");
    }
    else {
      getLogger().debug("Sending HTTP 200...");
      Reply.sendHttpReply(socket.getOutputStream(), Reply.HTTP_200,
                          "Hello, World!");
    }
  }

  /**
   * Sets the container-supplied logger.
   */
  public void setLogger(Logger logger) {
    m_logger = logger;
  }

  /**
   * Return the logging channel assigned to us by the container.
   * @return the logging channel
   */
  private Logger getLogger() {
    return m_logger;
  }
}
