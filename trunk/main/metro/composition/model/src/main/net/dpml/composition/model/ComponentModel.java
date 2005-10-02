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

package net.dpml.composition.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.component.ActivationPolicy;
import net.dpml.composition.info.InfoDescriptor.LifestylePolicy;
import net.dpml.composition.info.InfoDescriptor.CollectionPolicy;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;

import net.dpml.transit.model.UnknownKeyException;

/**
 * The ComponentModel interface defines the remotely accessible aspects of a component
 * configuration.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ComponentDirective.java 2991 2005-07-07 00:00:04Z mcconnell@dpml.net $
 */
public interface ComponentModel extends Remote
{
   /**
    * Return the component implementation class name.
    *
    * @return the classname of the implementation 
    */
    String getImplementationClassName() throws RemoteException;
    
   /**
    * Return the activation policy for the component.
    * @return the activation policy value
    */
    ActivationPolicy getActivationPolicy() throws RemoteException;

   /**
    * Set the component activation policy to the supplied value.
    * @return the new activation policy
    */
    void setActivationPolicy( ActivationPolicy policy ) throws RemoteException;

   /**
    * Return the component lifestyle policy. The value return is a non-null
    * lifestyle policy established via overriding policy assignments and 
    * defaulting to the type lifestyle policy if no override is present.
    *
    * @return the lifestyle policy value
    */
    LifestylePolicy getLifestylePolicy() throws RemoteException;

   /**
    * Override the default lifestyle policy with the supplied lifestyle.
    * A null value will result in lifestyle policy selection based on the 
    * component type's preferred lifestyle policy.
    *
    * @param lifestyle the lifestyle policy
    */
    void setLifestylePolicy( LifestylePolicy lifestyle ) throws RemoteException;

   /**
    * Return the current component collection policy.  If null, the component
    * type collection policy will be returned.
    *
    * @return a HARD, WEAK, SOFT or SYSTEM
    */
    CollectionPolicy getCollectionPolicy() throws RemoteException;

   /**
    * Override the assigned collection policy.
    * @param policy the collection policy value
    */
    void setCollectionPolicy( CollectionPolicy policy ) throws RemoteException;

   /**
    * Return the current context model.
    *
    * @return the context model
    */
    ContextModel getContextModel() throws RemoteException;
    
   /**
    * Return the set of component model keys.
    * @return the component part keys
    */
    String[] getPartKeys() throws RemoteException;

   /**
    * Return the component model of an internal part referenced by the supplied key.
    * @return the internal part component model 
    */
    ComponentModel getComponentModel( String key ) throws UnknownKeyException, RemoteException;

    Configuration getConfiguration() throws RemoteException;

    Parameters getParameters() throws  RemoteException;
}

