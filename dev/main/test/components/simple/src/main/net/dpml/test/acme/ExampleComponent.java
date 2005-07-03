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
public class ExampleComponent implements Example
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;

   /**
    * The assigned context instance.
    */
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new ExampleComponent.
    *
    * @param logger the logging channel asigned by the container
    * @param context the assign component context
    */
    public ExampleComponent( final Logger logger, final Context context )
    {
        m_context = context;
        m_logger = logger;
    }

    //------------------------------------------------------------------
    // Service interface
    //------------------------------------------------------------------

   /**
    * Implementation of the Example service interface.
    */
    public void doMyStuff()
    {
        Dimension defaultValue = new DimensionValue( 10, 20 );
        Dimension dimension = m_context.getDimension( defaultValue );
        int size = dimension.getSize();

        m_logger.info( 
          "Creating a widget with a area of " + size );
    }

    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Context
    {
        Dimension getDimension( Dimension d );
    }
}
