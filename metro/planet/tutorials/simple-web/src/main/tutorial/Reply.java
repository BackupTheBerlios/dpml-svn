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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Static-only class to send a simple HTTP reply.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class Reply {
  /**
   * Definition of a carriage-return/line-feed entity.
   */
  public final static String CRLF = "\r\n";
  /**
   * The HTTP RFC specification version supported by this web server.
   */
  public final static String HTTP_VERSION = "HTTP/1.0";
  /**
   * The HTTP RFC specification version supported by this web server.
   */
  public final static String WEB_SERVER_NAME = "Merlin Simple Server/1.0";
  /**
   * HTTP 200 OK
   */
  public final static String HTTP_200 = HTTP_VERSION + " 200 OK" + CRLF;
  /**
   * HTTP 400 Bad Request
   */
  public final static String HTTP_400 = HTTP_VERSION + " 400 Bad Request" +
      CRLF;
  /**
   * HTTP 403 Forbidden
   */
  public final static String HTTP_403 = HTTP_VERSION + " 404 Forbidden" + CRLF;
  /**
   * HTTP 404 Not Found
   */
  public final static String HTTP_404 = HTTP_VERSION + " 404 Not Found" + CRLF;
  /**
   * HTTP 405 Method Not Allowed
   */
  public final static String HTTP_405 = HTTP_VERSION +
      " 405 Method Not Allowed" + CRLF;

  /**
   * Private constructor to enforce static-only class.
   */
  private Reply() {
  }

  /**
   *
   * @param os
   * @param type
   * @param msg
   * @throws IOException
   */
  public static void sendHttpReply(OutputStream os, String type, String msg) throws
      IOException {

    DataOutputStream dos = new DataOutputStream(os);
    dos.writeBytes(type);
    dos.writeBytes("Connection: close" + CRLF);
    dos.writeBytes("Server: " + WEB_SERVER_NAME + CRLF);
    dos.writeBytes("Date: " + RfcDateFormat.format(new Date()) + CRLF);
    dos.writeBytes("Content-Type: text/html; charset=iso-8859-1" + CRLF);
    //dos.writeBytes("Content-Type: text/html" + CRLF);
    dos.writeBytes("Content-Length: " + msg.length() + CRLF);
    if (type.equals(HTTP_405)) {
      dos.writeBytes("Allow: GET" + CRLF);
    }
    dos.writeBytes(CRLF);

    String s = new String("<html><body><h1>" + msg + "</h1></body></html>");
    //dos.writeBytes(s);
    dos.writeUTF(s);
    dos.flush();
    dos.close();
  }
}
