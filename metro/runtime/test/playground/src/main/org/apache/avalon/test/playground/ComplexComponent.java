/* 
 * Copyright 2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.test.playground;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.test.playground.basic.BasicService;

/**
 * This is a demonstration component that declares no interface but
 * has dependecies on two services.  These include SimpleService and
 * BasicService.
 *
 * @avalon.component name="complex" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.playground.ComplexService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ComplexComponent
        implements ComplexService, Startable, Disposable
{
    //-----------------------------------------------------------------
    // immutable
    //-----------------------------------------------------------------

    private final Logger m_logger;
    private final ServiceManager m_manager;
    private final SimpleService m_simple;
    private final BasicService m_basic;

    //-----------------------------------------------------------------
    // mutable
    //-----------------------------------------------------------------

    private Thread m_thread;
    protected boolean m_continue = false;

    //-----------------------------------------------------------------
    // constructor
    //-----------------------------------------------------------------

   /**
    * @avalon.logger name="internal" 
    * @avalon.dependency key="basic" 
    *    type="org.apache.avalon.test.playground.basic.BasicService" version="1.1"
    * @avalon.dependency key="simple" 
    *    type="org.apache.avalon.test.playground.SimpleService" 
    */
    public ComplexComponent( Logger logger, ServiceManager manager ) throws ServiceException
    {
        m_logger = logger;
        m_manager = manager;

        //
        // lookup the primary service
        //

        Object simple = null;
        try
        {
            simple = m_manager.lookup( "simple" );
            m_simple = (SimpleService) simple;
        }
        catch( ClassCastException cce )
        {
            final String error = 
              "Container supplied service for the key 'simple' is not an instance of SimpleService: ["
              + simple.getClass().getName() 
              + ", " 
              + simple.toString()
              + "].";
            throw new ServiceException( "simple", error, cce );
        }

        try
        {
            m_basic = (BasicService) m_manager.lookup( "basic" );
        }
        catch( ClassCastException cce )
        {
            final String error = 
              "Container supplied service for the key 'basic' is not an instance of BasicService.";
            throw new ServiceException( "simple", error, cce );
        }

        Logger child = getLogger().getChildLogger( "internal" );
        if( child.isInfoEnabled() )
        {
            child.debug( "ready" );
        }
    }

    //=======================================================================
    // Startable
    //=======================================================================

    /**
     * Start the component.
     * @exception Exception if an error eoccurs
     */
    public void start() throws Exception
    {
        getLogger().info( "starting" );
        m_continue = true;
        m_thread = new Thread(
                new Runnable()
                {
                    public void run()
                    {
                        while( m_continue )
                        {
                            try
                            {
                                Thread.sleep( 100 );
                            } catch( Throwable e )
                            {
                                // ignore it
                            }
                        }
                    }
                }
        );
        m_thread.start();
        getLogger().info( "started" );
    }

    /**
     * Stop the component.
     */
    public void stop()
    {
        getLogger().info( "stopping" );
        m_continue = false;
        try
        {
            m_thread.join();
        } catch( Throwable e )
        {
            // timeout
        }
    }


    //-----------------------------------------------------------------------
    // Disposable
    //-----------------------------------------------------------------------

    /**
     * Dispose of the component.
     */
    public void dispose()
    {
        if( getLogger().isInfoEnabled() )
        {
            getLogger().info( "dispose" );
        }

        m_manager.release( m_simple );
        m_manager.release( m_basic );
    }

    //-----------------------------------------------------------------------
    // internals
    //-----------------------------------------------------------------------

    private Logger getLogger()
    {
        return m_logger;
    }

}
