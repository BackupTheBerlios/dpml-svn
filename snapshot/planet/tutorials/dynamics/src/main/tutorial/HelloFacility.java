/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tutorial;

import java.io.File;

import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ComponentModel;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import org.apache.avalon.meta.info.ReferenceDescriptor;

/**
 * A demonstration of a facility that involves runtime creation
 * and deployment of other components.
 *
 * @avalon.component name="hello" lifestyle="singleton"
 * @avalon.attribute key="urn:composition:deployment.timeout" value="6000"
 */
public class HelloFacility 
  implements Executable
{

   //---------------------------------------------------------
   // immutable state
   //---------------------------------------------------------

  /**
   * The logging channel supplied by the container.
   */
   private final Logger m_logger;

   //---------------------------------------------------------
   // mutable state
   //---------------------------------------------------------

  /**
   * The containment model (establish via contexualization)
   */
   private ContainmentModel m_model;

   //---------------------------------------------------------
   // constructor
   //---------------------------------------------------------

  /**
   * Creation of a new hello facility.
   *
   * @param logger a logging channel
   * @param context the supplied context
   * @avalon.entry key="urn:composition:containment.model" 
   *    type="org.apache.avalon.composition.model.ContainmentModel" 
   * @exception ContextException if a contextualization error occurs
   */
   public HelloFacility( Logger logger, Context context )
     throws ContextException
   {
       m_logger = logger;
       m_model = 
         (ContainmentModel) context.get( 
           "urn:composition:containment.model" );
   }

   //---------------------------------------------------------
   // Executable
   //---------------------------------------------------------

  /**
   * Request for execution trigger by the container.  The implementation
   * uses the containment model supplied during the contextualization phase
   * to dynamically respove a reference to a deployment model capable of 
   * supporting the Widget service interface.  The implementation uses this
   * model to instantiate the instance.  Subsequent steps in the example 
   * show the decommissining of the widget model, the modification of the 
   * model state (buy updating the models configuration) and the 
   * recommissioning of the model.  Finally a new widget instance is 
   * resolved and we can see (via logging messages) that the widget behaviour
   * has been modified as a result of the modification to the configuration.
   * 
   * @exception Exception is a runtime error occurs
   */
   public void execute() throws Exception
   {
       getLogger().info( "looking for a widget" );

       //
       // create a reference to the widget service and resolve a reference
       // to a component model using the service reference
       //

       ReferenceDescriptor reference = new ReferenceDescriptor( Widget.class.getName() );
       ComponentModel model = (ComponentModel) m_model.getModel( reference );
       getLogger().info( "got a widget model: " + model );

       //
       // commission the model and resolve a component instance
       //

       getLogger().info( "commissioning the widget model" );
       model.commission();
       Widget widget = (Widget) model.resolve();
       getLogger().info( "got the widget: " + widget );

       getLogger().info( "releasing the widget" );
       model.release( widget );

       getLogger().info( "time for a change" );
       getLogger().info( "decommissioning the widget model" );
       model.decommission();

       //
       // create an alternative configuration and apply it to the 
       // widget model
       //

       getLogger().info( "building alternative configuration" );
       DefaultConfiguration message = new DefaultConfiguration( "message" );
       message.setValue( "bonjour!" );
       DefaultConfiguration config = new DefaultConfiguration( "config" );
       config.addChild( message );
       model.setConfiguration( config );

       //
       // redeploy the model and create a new instance
       //

       getLogger().info( "recommissioning the widget model" );
       model.commission();
       widget = (Widget) model.resolve();
       getLogger().info( "got the updated widget: " + widget );
       model.release( widget );
       model.decommission();

       //
       // that's enought playing around with configurations - lets 
       // grab a gizmo model and fiddle with its context
       //

       getLogger().info( "lets play with the gizmo" );
       reference = new ReferenceDescriptor( Gizmo.class.getName() );
       model = (ComponentModel) m_model.getModel( reference );
       getLogger().info( "got a gizmo model: " + model );

       //
       // override the standard home context entry with something
       // we have derived
       //

       getLogger().info( "building alternative context entry" );
       model.getContextModel().setEntry( 
         "urn:avalon:home", 
          new File( System.getProperty( "user.dir" ) ) );
       model.commission();
       Gizmo gizmo = (Gizmo) model.resolve();
       getLogger().info( "got gizmo: " + gizmo );
   }

   //---------------------------------------------------------
   // private
   //---------------------------------------------------------

   private Logger getLogger()
   {
       return m_logger;
   }
}
