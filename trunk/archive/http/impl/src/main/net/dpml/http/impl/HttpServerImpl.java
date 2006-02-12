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

import net.dpml.activity.Disposable;
import net.dpml.activity.Startable;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpService;

import org.mortbay.jetty.Server;

/**
 * HTTP server implementation.
 */
public class HttpServerImpl extends Server
    implements Startable, Disposable, HttpService
{
   /**
    * Component context.
    */
    public interface Context
    { 
       /**
        * Return the trace policy.
        * @param value the component defined default value
        * @return the resolved trace policy
        */
        boolean getTrace( boolean value );

       /**
        * Return the anonymous policy.
        * @param value the component defined default value
        * @return the resolved anonymous policy
        */
        boolean getAnonymous( boolean value );
        
       /**
        * Return the graceful-stop policy.
        * @param value the component defined default value
        * @return the resolved policy
        */
        boolean getGracefulStop( boolean value );
        
       /**
        * Return the number of requests per GC action.
        * @param value the component defined default value
        * @return the request count
        */
        int getRequestsPerGC( int value );
    }

    private Logger  m_logger;
    private boolean m_graceful;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    */
    public HttpServerImpl( Logger logger, Context context )
    {
        m_logger = logger;
        
        boolean trace = context.getTrace( false );
        setTrace( trace );

        boolean anonymous = context.getAnonymous( false );
        setAnonymous( anonymous );

        m_graceful = context.getGracefulStop( false );

        int reqs = context.getRequestsPerGC( -1 );
        if( reqs > 0 )
        {
            setRequestsPerGC( reqs );
        }
    }

   /**
    * Dispose of the server.
    */
    public void dispose()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Disposing server: " + this );
        }
        super.destroy();
    }
}
