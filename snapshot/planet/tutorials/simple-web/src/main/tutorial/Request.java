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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a parser for an inbound HTTP connection.  The parser uses regular
 * expressions to examine the action line and host header line.  All other headers
 * and text are ignored.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class Request {

  static class Action {
    private String m_name;
    static final Action GET = new Action("GET");
    static final Action POST = new Action("POST");
    static final Action PUT = new Action("PUT");
    static final Action HEAD = new Action("HEAD");

    private Action(String name) {
      m_name = name;
    }

    public String toString() {
      return m_name;
    }

    static Action parse(String s) throws IllegalArgumentException {
      if (s.equals("GET")) {
        return GET;
      }
      if (s.equals("POST")) {
        return POST;
      }
      if (s.equals("PUT")) {
        return PUT;
      }
      if (s.equals("HEAD")) {
        return HEAD;
      }
      throw new IllegalArgumentException(s);
    }
  }

  private Action m_action;
  private String m_version;
  private URI m_uri;

  private Request(Action action, String version, URI uri) {
    setAction(action);
    setVersion(version);
    setUri(uri);
  }

  private static Charset ascii = Charset.forName("US-ASCII");

  private static Pattern requestPattern = Pattern.compile(
      "\\A([A-Z]+) +([^ ]+) +HTTP/([0-9\\.]+)$"
      + ".*^Host: ([^ ]+)$.*\r\n\r\n", Pattern.MULTILINE | Pattern.DOTALL);

  public static Request parse(ByteBuffer bb) throws MalformedRequestException {
    CharBuffer cb = ascii.decode(bb);

    Matcher m = requestPattern.matcher(cb);
    if (!m.matches()) {
      throw new MalformedRequestException();
    }

    Action a;
    try {
      a = Action.parse(m.group(1));
    }
    catch (IllegalArgumentException e) {
      throw new MalformedRequestException(e);
    }

    URI u;
    try {
      u = new URI("http://" + m.group(4) + m.group(2));
    }
    catch (URISyntaxException e) {
      throw new MalformedRequestException(e);
    }

    return new Request(a, m.group(3), u);
  }

  public Action getAction() {
    return m_action;
  }

  public String getVersion() {
    return m_version;
  }

  public URI getUri() {
    return m_uri;
  }

  public boolean isGet() {
    if (m_action.toString().equals("GET")) {
      return true;
    }
    else {
      return false;
    }
  }

  private void setAction(Action action) {
    m_action = action;
  }

  private void setVersion(String version) {
    m_version = version;
  }

  private void setUri(URI uri) {
    m_uri = uri;
  }
}
