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

/** Wrapper for the Jetty ClientCertAuthenticator
 *
 * @avalon.component name="http-authenticator-clientcert" lifestyle="singleton"
 * @avalon.service type="org.mortbay.http.Authenticator"
 */
public class ClientCertAuthenticator extends org.mortbay.http.ClientCertAuthenticator
    implements Parameterizable
{
    public ClientCertAuthenticator()
    {
    }
    
    public void parameterize( Parameters params )
        throws ParameterException
    {
        int maxHandshakeSec = params.getParameterAsInteger( "max-handshake-sec", -1 );
        if( maxHandshakeSec >= 0 )
            setMaxHandShakeSeconds( maxHandshakeSec );
    }
} 
 
