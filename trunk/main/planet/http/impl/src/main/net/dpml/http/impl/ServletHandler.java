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

/**
 * Jetty ServletHandler wrapper.
 */
public class ServletHandler extends org.mortbay.jetty.servlet.ServletHandler
    implements Startable, net.dpml.http.ServletHandler
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Return the handler name.
        * @return the handler name
        */
        String getName();
        
       /**
        * Return the assigned http context.
        * @return the http context
        */
        HttpContextService getHttpContext();
        
       /**
        * Return the handler index.
        * @param value the default value
        * @return the index
        */
        int getHandlerIndex( int value );
        
       /**
        * Return the session manager.
        * @param value the default value
        * @return the resolved session manager
        */
        SessionManager getSessionManager( SessionManager value );
        
       /**
        * Return the using-cookies policy.
        * @param value the default policy
        * @return the resolved cookie policy
        */
        boolean getUsingCookies( boolean value );
        
       /**
        * Return the auto-initialize servlets policy.
        * @param value the default policy
        * @return the resolved policy
        */
        boolean getAutoInitializeServlets( boolean value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Creation of a new ServletHandler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @param conf supplimentary configuration
    * @exception ConfigurationException if a configuration error occurs
    */
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
        for( int i=0; i<children.length; i++ )
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

   /**
    * Start the handler.
    * @exception Exception if a startup error occurs
    */
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
        if( !isStarted() )
        {
            super.doStart();
        }
    }
    
   /**
    * Stop the handler.
    * @exception Exception if an error occurs
    */
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

   /** 
    * Adds the contextObject into the ServletContext object.
    * @param entryName the entry name
    * @param contextObject the context object
    */
    public void addServletContextEntry( String entryName, Object contextObject )
    {
        ServletContext ctx = getServletContext();
        ctx.setAttribute( entryName, contextObject );
    }
}

