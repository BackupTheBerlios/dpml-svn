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
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.test.playground.basic.BasicService;

/**
 * This is a minimal demonstration component that a dependency on
 * BasicService and provides SimpleService.
 *
 * @avalon.component name="simple" lifestyle="singleton"
 * @avalon.stage id="urn:avalon.test.playground:exploitable"
 * @avalon.stage id="urn:avalon.test.playground:demonstratable"
 * @avalon.service type="org.apache.avalon.test.playground.SimpleService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class SimpleComponent extends AbstractLogEnabled
        implements Configurable, Serviceable, Initializable, Startable, SimpleService,
        Exploitable, Demonstratable, Disposable
{

    private String m_message;
    private BasicService m_basic;
    private Thread m_thread;
    protected boolean m_continuation = false;

    //=======================================================================
    // Configurable
    //=======================================================================

    /**
     * Configurate the component.
     * @param config the configuration
     */
    public void configure( Configuration config )
    {
        getLogger().info( "configure" );
        m_message = config.getChild( "message" ).getValue( null );
    }

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Service the component.
     * @param manager the service manager holding the depedent services
     * @avalon.dependency type="org.apache.avalon.test.playground.basic.BasicService" 
     *   version="1.1" key="basic"
     * @exception ServiceException if a service error occurs
     */
    public void service( ServiceManager manager ) throws ServiceException
    {
        getLogger().info( "service" );
        m_basic = (BasicService) manager.lookup( "basic" );
        manager.release( m_basic );
    }

    //=======================================================================
    // Exploitable
    //=======================================================================

    /**
     * The create stage interface implementation for the Exloitable extension.
     */
    public void incarnate()
    {
        getLogger().info( "incarnation stage" );
    }

    /**
     * The destroy stage interface implementation for the Exloitable extension.
     */
    public void etherialize()
    {
        getLogger().info( "etherialize stage" );
    }


    //=======================================================================
    // Demonstratable
    //=======================================================================

    /**
     * Prints out the supplied message.
     * @param message the message to print
     */
    public void demo( String message )
    {
        getLogger().info( "handling demonstratable stage: " + message );
    }

    //=======================================================================
    // Initializable
    //=======================================================================

    /**
     * Initialization of the component.
     */
    public void initialize()
    {
        getLogger().info( "initialize" );
        getLogger().debug( "context: " + Thread.currentThread().getContextClassLoader() );
        doObjective();
    }

    //=======================================================================
    // Startable
    //=======================================================================

    /**
     * Starts the component.
     * @exception Exception if an error occurs
     */
    public void start() throws Exception
    {
        getLogger().debug( "starting" );
        m_continuation = true;
        m_thread = new Thread(
                new Runnable()
                {
                    public void run()
                    {
                        while( m_continuation )
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
        getLogger().debug( "started" );
    }

    /**
     * Stops the component.
     */
    public void stop()
    {
        getLogger().info( "stopping" );
        m_continuation = false;
        try
        {
            m_thread.join();
        } catch( Throwable e )
        {
            // timeout
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

    /**
     * Dispose of the component.
     */
    public void dispose()
    {
        getLogger().info( "dispose" );
    }

    //=======================================================================
    // PrimaryService
    //=======================================================================

    /**
     * Prints out the message derived from the configuration.
     */
    public void doObjective()
    {
        getLogger().info( "handling service operation: " + m_message );
    }

}
