/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005 Stephen McConnell.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.http.spi;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;

/**
 * Defintion of the HttpService service contract.
 */
public interface HttpService
{
   /**
    * Add a context to the HTTP service.
    * @param context the context to add
    * @return the context
    */
    HttpContext addContext( HttpContext context );
    
   /**
    * Remove a context from the HTTP service.
    * @param context the context to remove
    * @return true if the context was removed
    */
    boolean removeContext( HttpContext context );
    
   /**
    * Return a context matching the supplied virtual host and path.
    * @param virtualhost the virtual host
    * @param contextPath the context path
    * @return the context
    */
    HttpContext getContext( String virtualhost, String contextPath );
    
   /**
    * Add a context listener.
    * @param listener the context listener
    * @return the listener
    */
    HttpListener addListener( HttpListener listener );
    
   /**
    * Remove a context listener.
    * @param listener the context listener
    */
    void removeListener( HttpListener listener );
    
   /**
    * Add a user realm.
    * @param realm the user realm
    * @return the realm
    */
    UserRealm addRealm( UserRealm realm );
    
   /**
    * Remove a user realm.
    * @param realmname the user realm
    * @return the realm
    */
    UserRealm removeRealm( String realmname );

   /**
    * Return the requrest log.
    * @return the request log
    */
    RequestLog getRequestLog();
    
   /**
    * Set the request log.
    * @param log the request log
    */
    void setRequestLog( RequestLog log );
    
}
