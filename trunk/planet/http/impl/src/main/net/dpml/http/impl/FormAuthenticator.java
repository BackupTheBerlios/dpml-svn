/*
 * Copyright 2004 Niclas Hedman.
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

import net.dpml.parameters.ParameterException;
import net.dpml.parameters.Parameters;

/** Wrapper for the Jetty FormAuthenticator.
 *
 * @metro.component name="http-authenticator-form" lifestyle="singleton"
 * @metro.service type="org.mortbay.http.Authenticator"
 */
public class FormAuthenticator extends org.mortbay.jetty.servlet.FormAuthenticator
{
    public FormAuthenticator( Parameters params )
    {
        String loginPage = params.getParameter( "login-page", null );
        if( loginPage != null )
            setLoginPage( loginPage );
        
        String errorPage = params.getParameter( "error-page", null );
        if( errorPage != null )
            setErrorPage( errorPage );
    }
} 
 
