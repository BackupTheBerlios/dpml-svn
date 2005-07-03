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
import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.logging.Logger;
import net.dpml.http.HttpContextService;

/**
 * @metro.component name="http-forward-handler" lifestyle="singleton"
 * @metro.service   type="org.mortbay.http.HttpHandler"
 */
public class ForwardHandler
    extends org.mortbay.http.handler.ForwardHandler
    implements Startable
{
    public interface Context
    {
        HttpContextService getHttpContext();
        int getHandlerIndex( int value );
        String getName();
        String getRootForward( String value );
        boolean getHandleQueries( boolean value );
    }

    private Logger              m_logger;
    private HttpContextService  m_context;
    private int                 m_index;

    /**
     * @metro.logger name="http"
     * @metro.entry key="urn:metro:name"
     *               type="java.lang.String"
     * @metro.dependency type="net.dpml.http.HttpContextService"
     *                    key="http-context"
     */
    public ForwardHandler(Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        m_index = context.getHandlerIndex( -1 );
        String name = context.getName();
        setName( name );
        
        String rootForward = context.getRootForward( null );
        if( rootForward != null )
        {
            setRootForward( rootForward );
        }

        boolean queries = context.getHandleQueries( false );
        setHandleQueries( queries );

        Configuration child = conf.getChild( "forwards" );
        configureForwards( child );
    }

    private void configureForwards( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] children = conf.getChildren( "forward" );
        for( int i=0 ; i < children.length ; i++ )
            configureForward( children[i] );
    }

    private void configureForward( Configuration conf )
        throws ConfigurationException
    {
        Configuration oldPath = conf.getChild( "from" );
        Configuration newPath = conf.getChild( "to" );
        addForward( oldPath.getValue(), newPath.getValue() );
    }

    public void start()
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
            m_logger.debug( "Starting ForwardHandler: " + this );
        }
        if( ! isStarted() )
        {
            super.start();
        }
    }

    public void stop()
        throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping ForwardHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}

