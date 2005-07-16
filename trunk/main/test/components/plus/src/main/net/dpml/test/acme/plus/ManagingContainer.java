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

import java.net.URI;
import java.util.logging.Logger;

import net.dpml.part.control.Component;
import net.dpml.part.service.Manageable;
import net.dpml.part.state.State;

import net.dpml.activity.Startable;

/**
 * Demonstration of a component that manages the state of a contained part.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ManagingContainer implements Startable
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;
    private final Parts m_parts;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a composite component. This implementation demonstrates
    * access to parts within itself and invocation of service and non-service
    * operations on its parts.
    *
    * @param logger the logging channel asign by the container
    */
    public ManagingContainer( final Logger logger, Parts parts )
    {
         m_logger = logger;
         m_parts = parts;
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

    public synchronized void start() throws Exception
    {
         Parts parts = getParts();
         Manageable component = (Manageable) parts.getTestComponent();
         component.initialize();         
         component.apply( "start" );
         component.execute( "audit" );
    }

    public synchronized void stop() throws Exception
    {
         Parts parts = getParts();
         Manageable component = (Manageable) parts.getTestComponent();
         component.terminate();
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
