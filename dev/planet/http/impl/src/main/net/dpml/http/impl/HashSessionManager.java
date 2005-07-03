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

import net.dpml.activity.Startable;
import net.dpml.parameters.ParameterException;
import net.dpml.parameters.Parameterizable;
import net.dpml.parameters.Parameters;

/**
 * @metro.component name="http-session-manager" lifestyle="singleton"
 * @metro.service   type="org.mortbay.jetty.servlet.SessionManager"
 */
public class HashSessionManager
    extends org.mortbay.jetty.servlet.HashSessionManager
    implements Startable
{

    public HashSessionManager( Parameters params )
    {
        int maxInactiveInterval = params.getParameterAsInteger( "max-inactive-interval", -1 );
        if( maxInactiveInterval >= 0 )
            setMaxInactiveInterval( maxInactiveInterval );
            
        int scavangePeriod = params.getParameterAsInteger( "scavange-period", -1 );
        if( scavangePeriod >= 0 )
            setScavengePeriod( scavangePeriod );

        boolean useRequestedId = params.getParameterAsBoolean( "use-requested-id", false );
        setUseRequestedId( useRequestedId );
        
        String workerName = params.getParameter( "worker-name", null );
        if( workerName != null )
            setWorkerName( workerName );
    }
}
 
