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

import java.util.Map;
import java.util.logging.Logger;

/**
 * Component implementation that demonstrates the use of a context inner-class. 
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ExampleContainer implements Example
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

   /**
    * The assigned context instance.
    */
    private final Parts m_parts;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new ExampleComponent.
    *
    * @param logger the logging channel asigned by the container
    * @param context the assign component context
    */
    public ExampleContainer( final Logger logger, final Context context, final Parts parts )
    {
        m_logger = logger;
        m_context = context;
        m_parts = parts;
    }

    //------------------------------------------------------------------
    // Service interface
    //------------------------------------------------------------------

   /**
    * Implementation of the Example service interface.
    */
    public void doMyStuff()
    {
        //
        // retrive and configure the 'dimension' parts' context using 
        // width and height values in our context
        //

        int width = m_context.getWidth( 9 );
        int height = m_context.getHeight( 7 );
        DimensionalContext.Manager manager = 
          m_parts.getDimensionContextManager();
        manager.setHeight( height );
        manager.setWidth( width );

        //Map map = m_parts.getDimensionContextMap();
        //map.put( "height", new Integer( height ) );
        //map.put( "width", new Integer( width ) );

        Dimension dimension = m_parts.getDimension();
        int size = dimension.getSize();
        m_logger.info( 
          "Creating a widget with a area of " + size );
    }

    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Context extends DimensionalContext{}

    public interface Parts
    {
        DimensionalContext.Manager getDimensionContextManager();
        Map getDimensionContextMap();
        Dimension getDimension();
    }
}
