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
 * Jetty NotFoundHandler wrapper.
 */
public class NotFoundHandler
    extends org.mortbay.http.handler.NotFoundHandler
    implements Startable
{
   /**
    * Deployment context.
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
        HttpContextService  getHttpContext();
        
       /**
        * Return the handler index.
        * @param value the default value
        * @return the index
        */
        int getHandlerIndex( int value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Creation of a new NotFoundHandler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    */
    public NotFoundHandler( Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );
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
            m_logger.debug( "Starting NotFoundHandler: " + this );
        }
        if( !isStarted() )
        {
            super.start();
        }
    }

   /**
    * Handle stop.
    * @exception InterruptedException if interrupted
    */
    public void doStop() throws InterruptedException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "Stopping NotFoundHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}
