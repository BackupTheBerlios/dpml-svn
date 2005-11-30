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

import net.dpml.activity.Startable;

/**
 */
public class HashSessionManager
    extends org.mortbay.jetty.servlet.HashSessionManager implements Startable
{
    public interface Context
    {
        int getMaxInactiveInterval( int value );
        int getScavengePeriod( int value );
        boolean getUseRequestedId( boolean flag );
        String getWorkerName( String name );
    }

    public HashSessionManager( Context context )
    {
        int maxInactiveInterval = context.getMaxInactiveInterval( -1 );
        if( maxInactiveInterval > -1 )
        {
            setMaxInactiveInterval( maxInactiveInterval );
        }
   
        int scavangePeriod = context.getScavengePeriod( -1 );
        if( scavangePeriod > -1 )
        {
            setScavengePeriod( scavangePeriod );
        }

        boolean useRequestedId = context.getUseRequestedId( false );
        setUseRequestedId( useRequestedId );
        
        String workerName = context.getWorkerName( null );
        if( workerName != null )
        {
            setWorkerName( workerName );
        }
    }
}
 
