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
 * Expiry handler.
 */
public class ExpiryHandler 
    extends org.mortbay.http.handler.ExpiryHandler
    implements Startable
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the handler name.
        * @return the name
        */
        String getName();
        
       /**
        * Get the handler index.
        * @param value the default index value
        * @return the index
        */
        int getHandlerIndex( int value );
        
       /**
        * Get the HTTP context.
        * @return the HTTP context
        */
        HttpContextService getHttpContext();
        
       /**
        * Get the time-to-live value
        * @param value the default value
        * @return the index
        */
        int getTimeToLive( int value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Create a new handler instance.
    * @param logger the logging channel
    * @param context the component context
    */
    public ExpiryHandler( Logger logger, Context context )
    {
        m_logger = logger;

        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );
        int timeToLive = context.getTimeToLive( -1 );
        if( timeToLive > 0 )
        {
            setTimeToLive( timeToLive );
        }
        m_context = context.getHttpContext();
    }

   /**
    * Start the handler.
    * @exception Exception if a startup error occurs
    */
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
            m_logger.debug( "Starting ExpiryHandler: " + this );
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
            m_logger.debug( "Stopping ExpiryHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}

