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

package test.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;

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
public class TestComponent
    implements LogEnabled, Serviceable, Configurable, HttpRequestHandler
{
    //----------------------------------------------------------
    // state
    //----------------------------------------------------------

    private Logger m_logger;

    private int m_count;

    private Counter m_counter;

    private String m_link;

    private String m_message;

    private String m_name;

    //----------------------------------------------------------
    // lifecycle
    //----------------------------------------------------------

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Service the servlet.
    * @param manager the service manager
    * @avalon.dependency type="test.http.Counter" key="counter"
    */
    public void service( ServiceManager manager ) throws ServiceException
    {
        getLogger().info( "servicing " + new Date() );
        m_counter = (Counter) manager.lookup( "counter" );
    }

    public void configure( Configuration config ) throws ConfigurationException
    {
        m_link = config.getChild( "link" ).getValue();
        m_message = config.getChild( "message" ).getValue();
        m_name = config.getChild( "name" ).getValue();
    }


    public void handle( String path, String params, 
                        HttpRequest request, HttpResponse response )
        throws IOException
    {
        m_logger.debug( "TestComponent - Handling HTTP: " + path );
        
        int count = m_counter.increment();
        
        m_count++;

        response.setContentType("text/html");
        OutputStream out = response.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter( out, "ISO8859-1" );
        PrintWriter writer = new PrintWriter( osw );
        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>" + m_name + "</title>" );
        writer.println("</head>");
        writer.println("<body>" );
        writer.println("<hr/>");
        writer.println("<h3>" 
          + m_name 
          + "</h3>");
        writer.println("<hr/>");
        writer.println("<P>" 
          + "local hits: " + m_count + "</br>"
          + "total hits: " + count + "</p>" );
        writer.println("<p>I'm an Avalon component.<br/>");
        writer.println(
           "Here is <a href=\"" 
           + m_link 
           + "\">a link to my mate.</a></p>");
        writer.println("<hr/>");
        writer.println("</body>");
        writer.println("</html>");
        writer.flush();
    }
}
