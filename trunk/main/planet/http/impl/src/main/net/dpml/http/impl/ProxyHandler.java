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
 * Jetty ProxyHandler wrapper.
 */
public class ProxyHandler
    extends org.mortbay.http.handler.ProxyHandler
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
        
       /**
        * Retrun the proxy host white list.
        * @param value the default white list value
        * @return the resolve white list value
        */
        String getProxyHostsWhiteList( String value );
        
       /**
        * Retrun the proxy host black list.
        * @param value the default black list value
        * @return the resolve black list value
        */
        String getProxyHostsBlackList( String value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Creation of a new ProxyHandler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    */
    public ProxyHandler( Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = context.getName();
        setName( name );
        m_index = context.getHandlerIndex( -1 );

        String whitelist = context.getProxyHostsWhiteList( null );
        if( whitelist != null )
        {
            String[] hosts = StringUtils.tokenize( whitelist );
            setProxyHostsWhiteList( hosts );
        }

        String blacklist = context.getProxyHostsBlackList( null );
        if( blacklist != null )
        {
            String[] hosts = StringUtils.tokenize( blacklist );
            setProxyHostsBlackList( hosts );
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
            m_logger.debug( "Starting ProxyHandler: " + this );
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
            m_logger.debug( "Stopping ProxyHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}

