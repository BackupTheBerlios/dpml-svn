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
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * A simple class composed of two parts (widget and gizmo). 
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class AcmeContainer implements Example
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;
    private final Context m_context;
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
    public AcmeContainer( final Logger logger, Context context, Parts parts )
    {
         m_logger = logger;
         m_context = context;
         m_parts = parts;
    }

    //------------------------------------------------------------------
    // Example service
    //------------------------------------------------------------------

    public void doMyStuff()
    {
        execute();
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

    public void execute()
    {
         Parts parts = getParts();

         //
         // get the gizmo's color context manager and play with some values
         //

         getLogger().info( "getting widget context manager" );
         ColorContext.Manager manager = parts.getGizmoContextManager();

         String color = manager.getColor( "purple" );
         manager.setColor( color );
         Gizmo gizmo = parts.getGizmo();
         gizmo.doGizmoStuff();

         // 
         // get the widget's generic context manager and assign a 
         // default dimension and update the gixmo component's 
         // context manager with the assignment of white as the 
         // preferred color
         //

         int width = m_context.getWidth( 1 );
         int height = m_context.getWidth( 5 );
         Dimension d = new DimensionValue( width, height );
         Map map = parts.getWidgetContextMap();
         map.put( "dimension", d );
         manager.setColor( "white" );
         gizmo.doGizmoStuff();

         //
         // play around with some of the other operations we declared on 
         // our parts interface
         //

         debug( "getting identified non-proxied gizmo" );
         Gizmo myProxiedGizmo = parts.getGizmo( true );

         debug( "cleaning up" );
         parts.releaseGizmo( gizmo );
         parts.releaseGizmo( myProxiedGizmo );
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

    public interface Context extends DimensionalContext
    {
    }

    public interface Parts
    {
       /**
        * Return an instance of widget (referenced by gizmo).
        * 
        * @return the widget service
        */
        Widget getWidget();

       /**
        * Return the widget's context manager.
        * 
        * @return the widget context manager
        */
        Map getWidgetContextMap();

       /**
        * Return an instance of gizmo using the lifestyle policy
        * declared by the gizmo component.
        * 
        * @return the gizmo service
        */
        Gizmo getGizmo();

       /**
        * Return the gizmo context manager.
        * 
        * @return the gizmo context manager
        */
        ColorContext.Manager getGizmoContextManager();

       /**
        * Return an instance of gizmo using a supplied proxy policy.
        * 
        * @param key the instance identifier
        * @return the gizmo service
        */
        Gizmo getGizmo( boolean policy );

       /**
        * Optional release of an instance of the gizmo component.
        * 
        * @param the gizmo to release
        */
        void releaseGizmo( Gizmo gizmo );

    }

    private void debug( String message )
    {
        getLogger().fine( message );
    }
}
