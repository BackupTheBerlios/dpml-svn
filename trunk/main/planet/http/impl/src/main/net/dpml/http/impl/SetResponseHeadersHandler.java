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
import net.dpml.configuration.Configurable;
import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpContextService;

public class SetResponseHeadersHandler
    extends org.mortbay.http.handler.SetResponseHeadersHandler
    implements Startable
{
    public interface Context
    {
        String getName();
        HttpContextService  getHttpContext();
        int getHandlerIndex( int value );
    }

    private Logger              m_logger;
    private HttpContextService  m_context;
    private int                 m_index;

    public SetResponseHeadersHandler( Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );

        Configuration child = conf.getChild( "headers" );
        configureHeaders( child );
    }

    private void configureHeaders( Configuration conf )
        throws ConfigurationException
    {
        Configuration[] headers = conf.getChildren( "header" );
        for( int i=0 ; i < headers.length ; i++ )
            configureHeader( headers[i] );
    }

    private void configureHeader( Configuration conf )
        throws ConfigurationException
    {
        Configuration name = conf.getChild( "name" );
        Configuration value = conf.getChild( "value" );
        setHeaderValue( name.getValue(), value.getValue() );
    }

    public void start() throws Exception
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
            m_logger.debug( "Starting SetResponseHeadersHandler: " + this );
        }
        if( ! isStarted() )
        {
            super.start();
        }
    }

    public void stop() throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping SetResponseHeadersHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}



