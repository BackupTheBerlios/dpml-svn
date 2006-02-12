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

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.SecurityConstraint;

/**
 * Http context service.
 */
public interface HttpContextService
{
   /**
    * Return the http context instance.
    * @return the context
    */
    HttpContext getHttpContext();
    
   /**
    * Add a security constraint.
    * @param path the path
    * @param sc the security constraint
    */
    void addSecurityConstraint( String path, SecurityConstraint sc );
    
   /**
    * Add a HTTP handler to the context.
    * @param handler the HTTP Handler 
    */
    void addHandler( HttpHandler handler );
    
   /**
    * Add a HTTP handler to the context using a specificed index.
    * @param index the handler index 
    * @param handler the HTTP Handler 
    */
    void addHandler( int index, HttpHandler handler );
    
   /**
    * Remove a handler from the context.
    * @param handler the HTTP Handler 
    */
    void removeHandler( HttpHandler handler );
    
   /**
    * Set the handler authenticator.
    * @param authenticator the authenticator 
    */
    void setAuthenticator( Authenticator authenticator );

   /**
    * Return the current authenticator.
    * @return the authenticator
    */
    Authenticator getAuthenticator();
}
