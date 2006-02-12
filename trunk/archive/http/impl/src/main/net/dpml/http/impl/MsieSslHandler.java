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

/**
 * Jetty MsieSslHandler wrapper.
 */
public class MsieSslHandler
    extends org.mortbay.http.handler.MsieSslHandler
    implements Startable
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Return the assigned http context.
        * @return the http context
        */
        HttpContextService getHttpContext();
        
       /**
        * Return the handler name.
        * @return the handler name
        */
        String getName();
        
       /**
        * Return the hqandler index.
        * @param value the default value
        * @return the index
        */
        int getHandlerIndex( int value );
        
       /**
        * Return the user agent substring.
        * @param value the default value
        * @return the user agent substring
        */
        String getUserAgentSubstring( String value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Creation of a new MsieSslHandler adapter.
    * @param logger the assigned logging channel
    * @param context the deployment context
    */
    public MsieSslHandler( Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = (String) context.getName();
        setName( name );
        
        m_index = context.getHandlerIndex( -1 );

        String agent = context.getUserAgentSubstring( null );
        if( agent != null )
        {
            setUserAgentSubString( agent );
        }
    }

   /**
    * Start the handler.
    * @exception Exception if a startup error occurs
    */
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
            m_logger.debug( "Starting MsieSslHandler: " + this );
        }
        if( !isStarted() )
        {
            super.start();
        }
    }

   /**
    * Stop the handler.
    * @exception InterruptedException if interrupted
    */
    public void stop() throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping MsieSslHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}

