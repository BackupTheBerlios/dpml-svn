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
import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.logging.Logger;
import net.dpml.http.spi.HttpContextService;

/**
 * Forward handler.
 */
public class ForwardHandler
    extends org.mortbay.http.handler.ForwardHandler
    implements Startable
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the HTTP context.
        * @return the HTTP context
        */
        HttpContextService getHttpContext();
        
       /**
        * Get the handler index.
        * @param value the default index value
        * @return the index
        */
        int getHandlerIndex( int value );
        
       /**
        * Get the handler name.
        * @return the name
        */
        String getName();
        
       /**
        * Return the root forward path.
        * @param value the default value
        * @return the resolved value
        */
        String getRootForward( String value );
        
       /**
        * Get the handle queries policy
        * @param value the default value
        * @return the resolved value
        */
        boolean getHandleQueries( boolean value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Create a new forward handler instance.
    * @param logger the logging channel
    * @param context the component context
    * @param conf the supplimentary configuration
    * @exception ConfigurationException if a confguration error occurs
    */
    public ForwardHandler( Logger logger, Context context, Configuration conf )
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
        for( int i=0; i<children.length; i++ )
        {
            configureForward( children[i] );
        }
    }

    private void configureForward( Configuration conf )
        throws ConfigurationException
    {
        Configuration oldPath = conf.getChild( "from" );
        Configuration newPath = conf.getChild( "to" );
        addForward( oldPath.getValue(), newPath.getValue() );
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
            m_logger.debug( "Starting ForwardHandler: " + this );
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

