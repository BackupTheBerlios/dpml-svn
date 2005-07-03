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
 * AbstractCompositionTestCase
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class GizmoComponent implements Gizmo
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;

   /**
    * The assigned context.
    */
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new gizmo component. Gizmo has a dependency 
    * on the widget service (declared under the Context
    * inner interface). It is also a disposable component 
    * identified by the public dispose method).
    *
    * @param logger the logging channel assigned by the container
    * @param dependencies the requested dependencies
    */
    public GizmoComponent( Logger logger, Context context )
    {
        m_logger = logger;
        m_context = context;
    }

    //------------------------------------------------------------------
    // Gixmo
    //------------------------------------------------------------------

    public void doGizmoStuff()
    {
        Widget widget = m_context.getWidget();
        String color = m_context.getColor( "red" );
        widget.doWidgetStuff( color );
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

    public void dispose()
    {
        m_logger.fine( "disposal" );
    }

    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Context extends ColorContext
    {
        Widget getWidget();
    }
}
