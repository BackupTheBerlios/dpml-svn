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

import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.http.HttpContextService;

/**
 * @avalon.component name="http-security-handler" lifestyle="singleton"
 * @avalon.service   type="org.mortbay.http.HttpHandler"
 */
public class SecurityHandler 
    extends org.mortbay.http.handler.SecurityHandler 
    implements Startable, Parameterizable, LogEnabled, 
               Serviceable, Contextualizable
{
    private Logger              m_Logger;
    private HttpContextService  m_Context;
    private int                 m_Index;
    
    public SecurityHandler()
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
    
    /**
     * Contextulaization of the Handler.
     *
     * @param ctx the supplied listener context
     *
     * @exception ContextException if a contextualization error occurs
     *
     * @avalon.entry key="urn:avalon:name" 
     *               type="java.lang.String" 
     */
    public void contextualize( Context ctx ) 
        throws ContextException
    {
        String name = (String) ctx.get( "urn:avalon:name" );
        setName( name );
    }

    public void parameterize( Parameters params )
        throws ParameterException
    {
        m_Index = params.getParameterAsInteger( "handler-index", -1 );
    }

    /**  
     * @avalon.dependency type="org.apache.avalon.http.HttpContextService"
     *                    key="http-context" 
     * @avalon.dependency type="org.mortbay.http.Authenticator"
     *                    key="authenticator" 
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_Context = (HttpContextService) man.lookup( "http-context" );
        
        // The dependency on Authenticator is to ensure that the Authenticator
        // has been assigned to the HttpContext before the SecurityHandler
        // is being started, since it otherwise will set another Authenticator.
    }
 
    public void start()
        throws Exception
    {
        if( m_Index >= 0 )
            m_Context.addHandler( m_Index, this );
        else
            m_Context.addHandler( this );
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Starting SecurityHandler: " + this );
        super.start();
    }
    
    public void stop()
        throws InterruptedException
    {
        if( m_Logger.isDebugEnabled() )
            m_Logger.debug( "Stopping SecurityHandler: " + this );
        super.stop();
        m_Context.removeHandler( this );
    }
} 
 
