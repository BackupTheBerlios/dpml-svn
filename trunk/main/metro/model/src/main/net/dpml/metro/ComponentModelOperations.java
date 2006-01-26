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

package net.dpml.metro;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.CategoryDirective;

import net.dpml.part.remote.Model;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;

import net.dpml.lang.UnknownKeyException;

/**
 * The ComponentModel interface defines the remotely accessible aspects of a component
 * configuration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentModelOperations
{
   /**
    * Return the component name.
    * @return the name
    * @exception RemoteException if a remote exception occurs
    */
    String getName() throws RemoteException;

   /**
    * Return the component implementation class name.
    *
    * @return the classname of the implementation 
    * @exception RemoteException if a remote exception occurs
    */
    String getImplementationClassName() throws RemoteException;
    
   /**
    * Return the component classloader directive.
    *
    * @return the classloader directive for the component
    * @exception RemoteException if a remote exception occurs
    */
    ClassLoaderDirective getClassLoaderDirective() throws RemoteException;
    
   /**
    * Return the component lifestyle policy.
    *
    * @return the lifestyle policy value
    * @exception RemoteException if a remote exception occurs
    */
    LifestylePolicy getLifestylePolicy() throws RemoteException;

   /**
    * Return the current component collection policy.  If null, the component
    * type collection policy will be returned.
    *
    * @return a HARD, WEAK, SOFT or SYSTEM
    * @exception RemoteException if a remote exception occurs
    */
    CollectionPolicy getCollectionPolicy() throws RemoteException;

   /**
    * Return the set of component model keys.
    * @return the component part keys
    * @exception RemoteException if a remote exception occurs
    */
    //String[] getPartKeys() throws RemoteException;

   /**
    * Return the component model of an internal part referenced by the supplied key.
    * @param key the part key
    * @return the internal part component model 
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote exception occurs
    */
    //ComponentModel getComponentModel( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Return the component configuration.
    * @return the configuration
    * @exception RemoteException if a remote exception occurs
    */
    Configuration getConfiguration() throws RemoteException;

   /**
    * Return the component parameters.
    * @return the parameters
    * @exception RemoteException if a remote exception occurs
    */
    Parameters getParameters() throws  RemoteException;
    
   /**
    * Return the component logging categories.
    * @return the categories
    * @exception RemoteException if a remote exception occurs
    */
    CategoryDirective[] getCategoryDirectives() throws  RemoteException;

}

