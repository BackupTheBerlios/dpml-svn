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

package org.apache.avalon.playground;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.http.HttpRequestHandler;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * HTTP Handler component that receives and processes http requests.
 * 
 * @avalon.component name="test" lifestyle="thread"
 * @avalon.service type="org.apache.avalon.http.HttpRequestHandler"
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class HelloComponent implements HttpRequestHandler
{
    //----------------------------------------------------------
    // state
    //----------------------------------------------------------

    private Logger m_logger;

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

    public HelloComponent( Logger logger )
    {
        m_logger = logger;
    }

    //----------------------------------------------------------
    // implemetation
    //----------------------------------------------------------

    protected Logger getLogger()
    {
        return m_logger;
    }


    public void handle( String path, String params, 
                        HttpRequest request, HttpResponse response )
        throws IOException
    {
        m_logger.debug( "Hello - Handling HTTP: " + path );
                
        response.setContentType("text/html");
        OutputStream out = response.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter( out, "ISO8859-1" );
        PrintWriter writer = new PrintWriter( osw );
        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>" + "Hello" + "</title>" );
        writer.println("</head>");
        writer.println("<body>" );
        writer.println("<hr/>");
        writer.println("<h3>Hello</h3>");
        writer.println("<hr/>");
        writer.println("</body>");
        writer.println("</html>");
        writer.flush();
    }
}
