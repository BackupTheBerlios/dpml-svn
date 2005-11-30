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

import javax.servlet.ServletContext;

import net.dpml.activity.Startable;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import net.dpml.logging.Logger;

import net.dpml.http.spi.HttpContextService;

import org.mortbay.jetty.servlet.SessionManager;

public class ServletHandler extends org.mortbay.jetty.servlet.ServletHandler
    implements Startable, net.dpml.http.ServletHandler
{
    public interface Context
    {
        HttpContextService getHttpContext();
        SessionManager getSessionManager( SessionManager value );
        String getName();
        int getHandlerIndex( int value );
        boolean getUsingCookies( boolean value );
        boolean getAutoInitializeServlets( boolean value );
    }

    private Logger              m_logger;
    private HttpContextService  m_context;
    private int                 m_index;

    public ServletHandler( Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        SessionManager sm = context.getSessionManager( null );
        if( null != sm )
        {
            setSessionManager( sm );
        }
        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );
        boolean useCookies = context.getUsingCookies( false );
        setUsingCookies( useCookies );
        boolean autoInitialize = context.getAutoInitializeServlets( true );
        setAutoInitializeServlets( autoInitialize );

        Configuration child = conf.getChild( "servlets" );
        configureServlets( child );
    }

    private void configureServlets( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "servlet" );
        for( int i = 0 ; i < children.length ; i++ )
        {
            configureServlet( children[i] );
        }
    }

    private void configureServlet( Configuration conf )
        throws ConfigurationException
    {
        String path = conf.getChild( "path" ).getValue();
        String classname = conf.getChild( "classname" ).getValue();
        String name = conf.getChild( "name" ).getValue( null );
        if( name == null )
        {
            addServlet( path, classname );
        }
        else
        {
            String forcedPath = conf.getChild( "forced" ).getValue( null );
            if( forcedPath == null )
            {
                addServlet( name, path, classname, forcedPath );
            }
            else
            {
                addServlet( name, path, classname );
            }
        }
    }

    protected void doStart()
        throws Exception
    {
        if( m_index >= 0 )
        {
            m_context.addHandler( m_index, this );
        }
        else
        {
            m_context.addHandler( this );
        }
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Starting ServletHandler: " + this );
        }
        if( ! isStarted() )
        {
            super.doStart();
        }
    }
    
    protected void doStop()
        throws Exception
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping ServletHandler: " + this );
        }
        if( isStarted() )
        {
            super.doStop();
        }
        m_context.removeHandler( this );
    }

    /** Adds the contextObject into the ServletContext object.
     *
     */
    public void addServletContextEntry( String entryName, Object contextObject )
    {
        ServletContext ctx = getServletContext();
        ctx.setAttribute( entryName, contextObject );
    }
}

