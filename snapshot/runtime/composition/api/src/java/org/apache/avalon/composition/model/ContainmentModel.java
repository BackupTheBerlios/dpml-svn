/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.composition.model;

import java.net.URL;
import java.util.List;

import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.TargetDirective;
import org.apache.avalon.composition.event.CompositionListener;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;

/**
 * Containment model is an extended deployment model that aggregates 
 * a set of models.  A containment model describes a logical containment 
 * context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContainmentModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ContainmentModel extends DeploymentModel
{
    String KEY = "urn:composition:containment.model";
    
    String SECURE_EXECUTION_KEY = "urn:composition:security.enabled";

    /**
     * Get the startup sequence for the model.
     */
    DeploymentModel[] getStartupGraph();

    /**
     * Get the shutdown sequence for the model.
     */
    DeploymentModel[] getShutdownGraph();

   /**
    * Return the partition established by the containment model.
    *
    * @return the partition name
    */
    String getPartition();

   /**
    * Return the classloader model.
    *
    * @return the classloader model
    */
    ClassLoaderModel getClassLoaderModel();
    
   /** 
    * Return the default deployment timeout value declared in the 
    * kernel configuration.  The implementation looks for a value
    * assigned under the property key "urn:composition:deployment.timeout"
    * and defaults to 1000 msec if undefined.
    *
    * @return the default deployment timeout value
    */
   long getDeploymentTimeout();

    /**
     * Assemble the containment model.
     * @exception Exception if an error occurs during model assembly
     */
    void assemble() throws AssemblyException;

    /**
     * Assemble the model.
     * @param subjects a list of deployment models that make up the assembly chain
     * @exception Exception if an error occurs during model assembly
     */
    void assemble( List subjects ) throws AssemblyException;

    /**
     * Disassemble the model.
     */
    void disassemble();

   /**
    * Return the set of models nested within this model.
    * @return the classloader model
    */
    DeploymentModel[] getModels();

   /**
    * Return a model relative to a supplied path.
    *
    * @param path a relative or absolute path
    * @return the model or null if the path is unresolvable
    * @exception IllegalArgumentException if the path if badly formed
    */
    DeploymentModel getModel( String path );

   /**
    * Resolve a model capable of supporting the supplied service reference.
    *
    * @param descriptor a service reference descriptor
    * @return the model or null if unresolvable
    * @exception AssemblyException if an assembly error occurs
    */
    DeploymentModel getModel( ReferenceDescriptor descriptor )
      throws AssemblyException;

   /**
    * Resolve a model capable of supporting the supplied service reference.
    *
    * @param dependency a service dependency descriptor
    * @return the model or null if unresolvable
    * @exception AssemblyException if an assembly error occurs
    */
    DeploymentModel getModel( DependencyDescriptor dependency )
      throws AssemblyException;

   /**
    * Addition of a new subsidiary containment model
    * using a supplied profile url.
    *
    * @param url a containment profile url
    * @return the model based on the derived profile
    * @exception ModelException if an error occurs during model establishment
    */
    ContainmentModel addContainmentModel( URL url ) throws ModelException;

   /**
    * Addition of a new subsidiary containment model within
    * the containment context using a supplied url.
    *
    * @param block a url referencing a containment profile
    * @param config containment configuration targets
    * @return the model created using the derived profile and configuration
    * @exception ModelException if an error occurs during model establishment
    */
    ContainmentModel addContainmentModel( URL block, URL config ) 
      throws ModelException;

   /**
    * Addition of a new subsidiary model within
    * the containment context using a supplied profile.
    *
    * @param profile a containment or deployment profile 
    * @return the model based on the supplied profile
    * @exception ModelException if an error occurs during model establishment
    */
    DeploymentModel addModel( DeploymentProfile profile ) throws ModelException;

   /**
    * Addition of a new subsidiary model within
    * the containment context.
    *
    * @param model the model to add 
    * @return the model 
    */
    DeploymentModel addModel( DeploymentModel model ) throws ModelException;

   /**
    * Remove a named model from this model.
    * @param name the name of an immediate child model
    */
    void removeModel( String name );

   /**
    * Return the set of service export models.
    * @return t he export directives
    */
    ServiceModel[] getServiceModels();

   /**
    * Return a service exoport model matching a supplied class.
    * @return the service model
    */
    ServiceModel getServiceModel( Class clazz );

   /**
    * Apply a set of override targets resolvable from a supplied url.
    * @param url a url resolvable to a TargetDirective[]
    * @exception ModelException if an error occurs
    */
    void applyTargets( URL url )
      throws ModelException;

   /**
    * Apply a set of override targets.
    * @param targets a set of target directives
    */
    void applyTargets( TargetDirective[]targets );

   /**
    * Add a composition listener to the model.
    * @param listener the composition listener
    */
    void addCompositionListener( CompositionListener listener );

   /**
    * Remove a composition listener from the model.
    * @param listener the composition listener
    */
    void removeCompositionListener( CompositionListener listener );
}
