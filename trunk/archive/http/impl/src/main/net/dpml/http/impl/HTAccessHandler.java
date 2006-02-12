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
 * Jetty HTAccessHandler wrapper.
 */
public class HTAccessHandler
    extends org.mortbay.http.handler.HTAccessHandler
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
        * Get the default access value.
        * @param value the default value
        * @return the resolved value
        */
        String getDefaultAccess( String value );

       /**
        * Get the access file path.
        * @param value the default value
        * @return the resolved value
        */
        String getAccessFile( String value );
    }

    private Logger m_logger;
    private HttpContextService m_context;
    private int m_index;

   /**
    * Creation of a new HTAccessHandler instance.
    * @param logger the assigned logging channel
    * @param context the deployment context
    */
    public HTAccessHandler( Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        m_index = context.getHandlerIndex( -1 );
        String name = context.getName();
        setName( name );
        
        String defaultAccess = context.getDefaultAccess( null );
        if( defaultAccess != null )
        {
            setDefault( defaultAccess );
        }

        String filename = context.getAccessFile( null );
        if( filename != null )
        {
            setAccessFile( filename );
        }
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
            m_logger.debug( "Starting HTAccessHandler: " + this );
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
            m_logger.debug( "Stopping HTAccessHandler: " + this );
        }
        if( isStarted() )
        {
            super.stop();
        }
        m_context.removeHandler( this );
    }
}

