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
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpContextService;
import net.dpml.http.spi.FileResourceHandler;

/**
 */
public class ResourceHandler
    extends org.mortbay.http.handler.ResourceHandler
    implements Startable, FileResourceHandler 
{
    public interface Context
    {
        String getName();
        HttpContextService getHttpContext();
        int getHandlerIndex( int value );
        boolean getAcceptRanges( boolean value );
        boolean getDirAllowed( boolean value );
        boolean getRedirectWelcome( boolean value );
        int getMinGzipLength( int value );
        String getAllowedMethods( String value );
    }

    private Logger              m_logger;
    private HttpContextService  m_context;
    private int                 m_index;

    public ResourceHandler(Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );

        boolean ranges = context.getAcceptRanges( false );
        setAcceptRanges( ranges );

        boolean dirAllowed = context.getDirAllowed( false );
        setDirAllowed( dirAllowed );

        boolean redirectWelcome = context.getRedirectWelcome( false );
        setRedirectWelcome( redirectWelcome );

        int minGzip = context.getMinGzipLength( -1 );
        if( minGzip > 0 )
        {
            setMinGzipLength( minGzip );
        }

        String allow = context.getAllowedMethods( null );
        if( allow != null )
        {
            String[] methods = StringUtils.tokenize( allow );
            setAllowedMethods( methods );
        }
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
            m_logger.debug( "Starting ResourceHandler: " + this );
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
            m_logger.debug( "Stopping ResourceHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}
