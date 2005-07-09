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

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Component implementation that demonstrates the use of a context inner-class. 
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class WidgetComponent implements Widget
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
    * A mutable name.
    */
    private String m_name;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new widget component. The component is assigned 
    * a logging channel and context by the container.  The context is 
    * pre-prepared by the container based on the criteria expressed in 
    * the Context inner interface.
    *
    * @param logger the logging channel asigned by the container
    * @param context the assign component context
    */
    public WidgetComponent( final Logger logger, final Context context )
    {
        m_context = context;
        m_logger = logger;

        m_name = context.getName();
        URI uri = context.getUri();
        File work = context.getWorkingDirectory();

        final String message = 
          "Widget created with a uri ["
          + uri 
          + "] and name ["
          + m_name 
          + "] with a working dir ["
          + work
          + "].";
        logger.fine( message );
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

   /**
    * Return the mutable name.
    * @return the component name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the mutable name.
    * @return the component name
    */
    public void setName( String name )
    {
        m_name = name;
    }

   /**
    * Disposal of the component.
    */
    public void dispose()
    {
        getLogger().fine( "disposal" );
    }

   /**
    * Test is this component instance is equal to the supplied
    * instance.  This implementation is primarily used in test cases
    * to establish instance equality and uniquness across different
    * proxies to the same service instance.
    */
    public boolean equals( Object other )
    {
        return ( this.hashCode() == other.hashCode() );
    }

    //------------------------------------------------------------------
    // Widget
    //------------------------------------------------------------------

   /**
    * Implementation of the widget service contract.
    */
    public void doWidgetStuff( final String color )
    {
        Dimension d = new DimensionValue( 10, 20 );
        int width = m_context.getDimension( d ).getWidth();
        int height = m_context.getDimension( d ).getHeight();

        getLogger().info( 
          "Creating "
          + color 
          + " widget with a width of " 
          + width 
          + " and a height of " 
          + height );
    }

    //------------------------------------------------------------------
    // internal
    //------------------------------------------------------------------

    private Logger getLogger()
    {
        return m_logger;
    }

    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Context extends ColorContext
    {
        String getName();
        URI getUri();
        File getWorkingDirectory();
        Dimension getDimension( Dimension d );
    }
}
