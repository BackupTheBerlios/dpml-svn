/* 
 * Copyright 1999-2004 Apache Software Foundation
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
 
package org.apache.avalon.http;

import java.io.IOException;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * Defines the method that all http handlers must implement.
 *
 * <p>A http handler is a component that runs within a  
 * container that implements support for HTTP requests and 
 * response generation.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface HttpRequestHandler
{
    /**
     * Called by the container to allow the handler to respond to 
     * a request in a manner that parrallels the Servlet model.
     * <p>This method is only called after the component has been fully deployed.
     *
     * @param request the <code>ServletRequest</code> object that contains
     *    the client's request
     *
     * @param response the <code>ServletResponse</code> object that contains
     *    the handler's response
     *
     * @exception ServletException if an exception occurs that interferes
     *   with the handlers normal operation 
     *
     * @exception IOException if an input or output exception occurs
     *
     */
    public void handle( String pathInContext, String pathParms,
                        HttpRequest request, HttpResponse response )
        throws HttpRequestHandlerException, IOException;
}
