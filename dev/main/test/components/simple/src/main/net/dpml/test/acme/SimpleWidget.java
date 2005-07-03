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

package net.dpml.test.acme;

import java.util.logging.Logger;

/**
 * Component implementation that demonstrates the use of a context inner-class. 
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class SimpleWidget implements Widget
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

    public SimpleWidget( Logger logger )
    {
        m_logger = logger;
    }
    
    //------------------------------------------------------------------
    // Widget
    //------------------------------------------------------------------

   /**
    * Implementation of the widget service contract.
    */
    public void doWidgetStuff( String color )
    {
        m_logger.info( "Creating a " + color + " widget." );
    }

}
