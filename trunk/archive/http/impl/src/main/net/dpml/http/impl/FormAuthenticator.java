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
package net.dpml.http.impl;

/** 
 * Wrapper for the Jetty FormAuthenticator.
 */
public class FormAuthenticator extends org.mortbay.jetty.servlet.FormAuthenticator
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the login page value.
        * @param value the default value
        * @return the resolved page value
        */
        String getLoginPage( String value );
        
       /**
        * Get the error page value.
        * @param value the default value
        * @return the resolved page value
        */
        String getErrorPage( String value );
    }

   /**
    * Create a new form authenticator.
    * @param context the component context
    */
    public FormAuthenticator( Context context )
    {
        String loginPage = context.getLoginPage( null );
        if( loginPage != null )
        {
            setLoginPage( loginPage );
        }
        String errorPage = context.getErrorPage( null );
        if( errorPage != null )
        {
            setErrorPage( errorPage );
        }
    }
}

