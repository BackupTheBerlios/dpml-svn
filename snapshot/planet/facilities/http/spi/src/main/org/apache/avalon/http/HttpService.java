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

package org.apache.avalon.http;


import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpServer;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;

/**
 * Defintion of the HttpService service contract.
 *
 * @avalon.service type="org.apache.avalon.http.HttpService" version="1.0"
 */
public interface HttpService
{

    HttpContext addContext( HttpContext context );
    
    boolean removeContext( HttpContext context );
    
    HttpContext getContext( String virtualhost, String contextPath );
    
    HttpListener addListener( HttpListener listener );
    
    void removeListener( HttpListener listener );
    
    UserRealm addRealm( UserRealm realm );
    
    UserRealm removeRealm( String realmname );

    RequestLog getRequestLog();
    
    void setRequestLog( RequestLog log );
    
}
