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
import net.dpml.configuration.Configurable;
import net.dpml.configuration.ConfigurationException;

import net.dpml.activity.Executable;

import net.dpml.component.control.Controller;
import net.dpml.component.Component;

/**
 * Demonstration of a component that uses a supplied configuration to 
 * configure its children.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ConfigurableContainer implements Executable
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;
    private final Parts m_parts;
    private final Configuration m_configuration;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * This container demonstrates how a supplied configuration can be 
    * used as the source for configuration of the model of a contained 
    * part.
    *
    * @param logger the logging channel asign by the container
    * @param parts the parts managed by this container
    * @param configuration the container configuration
    */
    public ConfigurableContainer( final Logger logger, Parts parts, Configuration configuration )
    {
         m_logger = logger;
         m_parts = parts;
         m_configuration = configuration;
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

    public void execute() throws Exception
    {
        Parts parts = getParts();
        Component component = parts.getTestComponent();
        if( component instanceof Configurable )
        {
            Configuration conf = m_configuration.getChild( "test", false );
            if( null != conf )
            {
                getLogger().info( "applying configuration to " + component );
                ((Configurable)component).configure( conf );
            }
        }
        component.resolve();
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private Parts getParts()
    {  
        return m_parts;
    }

    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Parts
    {
        Component getTestComponent();
    }
}
