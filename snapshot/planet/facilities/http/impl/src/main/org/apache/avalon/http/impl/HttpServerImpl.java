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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.http.HttpService;

import org.mortbay.http.HttpServer;
import org.mortbay.util.MultiException;

/** Wrapper for the Jetty HttpServer
 *
 * @avalon.component name="http-server" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.http.HttpService"
 */
public class HttpServerImpl extends HttpServer
    implements LogEnabled, Parameterizable, Startable, Disposable, HttpService
{
    private Logger      m_Logger;
    private boolean m_Graceful;

        
    public HttpServerImpl()
    {
    }
    
    /**
     * Enable the logging system.
     *
     * @avalon.logger name="http"
     */
    public void enableLogging( Logger logger )
    {
        m_Logger = logger;
    }
    
    public Logger getLogger()
    {
        return m_Logger;
    }
    
    public void parameterize( Parameters params )
        throws ParameterException
    {
        boolean trace = params.getParameterAsBoolean( "trace", false );
        setTrace( trace );
        
        boolean anonymous = params.getParameterAsBoolean( "anonymous", false );
        setAnonymous( anonymous );
        
        m_Graceful = params.getParameterAsBoolean( "graceful-stop", false );
        
        int reqs = params.getParameterAsInteger( "request-gc", -1 );
        if( reqs > 0 )
            setRequestsPerGC( reqs );
    }
    
    public void start()
        throws MultiException
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Starting server: " + this );
        super.start();
    }
    
    public void stop()
        throws InterruptedException
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Stopping server: " + this );
        super.stop( m_Graceful );
    }
    
    public void dispose()
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Disposing server: " + this );
        super.destroy();
    }
} 
