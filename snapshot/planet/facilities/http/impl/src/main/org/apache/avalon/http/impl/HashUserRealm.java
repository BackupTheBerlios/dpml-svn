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

import java.io.IOException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

/** Wrapper for the Jetty HashUserRealm.
 *
 * @avalon.component name="http-userrealm-hash" lifestyle="singleton"
 * @avalon.service type="org.mortbay.http.UserRealm"
 */
public class HashUserRealm extends org.mortbay.http.HashUserRealm
    implements Parameterizable
{
    public HashUserRealm()
    {
    }
    
    public void parameterize( Parameters params )
        throws ParameterException
    {
        String realmName = params.getParameter( "name" );
        setName( realmName );
        
        String filename = params.getParameter( "filename" );
        try
        {
            load( filename );
        } catch( IOException e )
        {
            throw new ParameterException( "Unable to read file: " + filename, e );
        }
    }    
} 
