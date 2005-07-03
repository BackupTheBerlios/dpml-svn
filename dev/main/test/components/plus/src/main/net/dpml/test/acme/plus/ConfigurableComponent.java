/*
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.test.acme.plus;

import java.util.logging.Logger;

import net.dpml.configuration.Configuration;

import net.dpml.activity.Executable;

/**
 * Experimental component dealing with state management.
 * 
 * @author <a href="mailto:info@dpml.net">The Digital Product Meta Library</a>
 */
public class ConfigurableComponent implements Executable
{
    // ------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------

    private final Logger m_logger;
    private final Configuration m_configuration;

    // ------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------

    /**
     * Creation of a component that uses a supplied configuration.
     * 
     * @param logger the logging channel assigned by the container
     * @param configuration the supplied component configuration
     */
    public ConfigurableComponent( final Logger logger, final Configuration configuration )
    {
        m_logger = logger;
        m_configuration = configuration;
    }

    // ------------------------------------------------------------------
    // activity
    // ------------------------------------------------------------------

    public void execute() throws Exception
    {
        Configuration color = m_configuration.getChild( "color" );
        String value = color.getValue( "undefined" );
        getLogger().info( "color: " + value ); 
    }

    // ------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------

    private Logger getLogger()
    {
        return m_logger;
    }
}
