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

import java.io.IOException;
import net.dpml.parameters.ParameterException;
import net.dpml.parameters.Parameterizable;
import net.dpml.parameters.Parameters;

/** Wrapper for the Jetty HashUserRealm.
 *
 * @metro.component name="http-userrealm-hash" lifestyle="singleton"
 * @metro.service type="org.mortbay.http.UserRealm"
 */
public class HashUserRealm extends org.mortbay.http.HashUserRealm
{
    public HashUserRealm( Parameters params )
        throws ParameterException
    {
        String realmName = params.getParameter( "name" );
        setName( realmName );

        String uri = params.getParameter( "realm-uri" );
        try
        {
            load( uri );
        } catch( IOException e )
        {
            throw new ParameterException( "Unable to read realm from " + uri, e );
        }
    }
}
