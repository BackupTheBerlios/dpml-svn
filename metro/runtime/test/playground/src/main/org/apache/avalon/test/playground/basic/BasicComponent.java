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

package org.apache.avalon.test.playground.basic;

import java.io.File;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.test.playground.NullService;

/**
 * This is a minimal demonstration component that implements the
 * <code>BasicService</code> interface and has no dependencies.
 *
 * @avalon.component name="basic" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.playground.basic.BasicService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class BasicComponent extends AbstractLogEnabled
   implements Contextualizable, Configurable, Initializable, 
   Startable, Disposable, BasicService, NullService
{

    private String m_location;
    private String m_message;
    private File m_home;
    private boolean m_started = false;

    //=======================================================================
    // Contextualizable
    //=======================================================================

    /**
     * Supply of the the component context to the component type.
     * @param context the context value
     * @avalon.entry key="location"
     * @avalon.entry key="home" type="java.io.File"
     * @avalon.context strategy="org.apache.avalon.framework.context.Contextualizable"
     */
    public void contextualize( Context context )
    {
        BasicContext c = (BasicContext) context;
        m_location = c.getLocation();
        m_home = c.getWorkingDirectory();
    }

    //=======================================================================
    // Configurable
    //=======================================================================

    /**
     * Supply of the the component configuration to the type.
     * @param config the configuration value
     */
    public void configure( Configuration config )
    {
        getLogger().info( "configure" );
        m_message = config.getChild( "message" ).getValue( null );
    }

    //=======================================================================
    // Initializable
    //=======================================================================

    /**
     * Initialization of the component type by its container.
     */
    public void initialize()
    {
        getLogger().info( "initialize" );
        getLogger().debug( "location: " + m_location );
        getLogger().debug( "home: " + m_home );
        getLogger().debug( "message: " + m_message );
    }

    //=======================================================================
    // Startable
    //=======================================================================

    /**
     * Start the component.
     */
    public void start()
    {
        if( !m_started )
        {
            getLogger().info( "starting" );
            doPrimeObjective();
            m_started = true;
        }
    }

    /**
     * Stop the component.
     */
    public void stop()
    {
        getLogger().info( "stopping" );
    }

    /**
     * Dispose of the component.
     */
    public void dispose()
    {
        getLogger().info( "dispose" );
    }

    //=======================================================================
    // BasicService
    //=======================================================================

    /**
     * Service interface implementation.
     */
    public void doPrimeObjective()
    {
        getLogger().info( m_message + " from '" + m_location + "'." );
    }

}
