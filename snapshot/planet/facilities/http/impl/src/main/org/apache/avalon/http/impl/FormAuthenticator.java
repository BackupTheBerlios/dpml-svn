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

package org.apache.avalon.http.impl;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

/** Wrapper for the Jetty FormAuthenticator.
 *
 * @avalon.component name="http-authenticator-form" lifestyle="singleton"
 * @avalon.service type="org.mortbay.http.Authenticator"
 */
public class FormAuthenticator extends org.mortbay.jetty.servlet.FormAuthenticator
    implements Parameterizable
{
    public void parameterize( Parameters params )
        throws ParameterException
    {
        String loginPage = params.getParameter( "login-page", null );
        if( loginPage != null )
            setLoginPage( loginPage );
        
        String errorPage = params.getParameter( "error-page", null );
        if( errorPage != null )
            setErrorPage( errorPage );
    }
} 
 
