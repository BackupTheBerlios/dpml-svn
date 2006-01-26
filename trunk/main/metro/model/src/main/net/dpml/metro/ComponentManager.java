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

import net.dpml.metro.info.CollectionPolicy;

import net.dpml.part.ActivationPolicy;

import net.dpml.lang.UnknownKeyException;

/**
 * The ComponentManager interface provides support for manipulatation of 
 * a local component model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentManager extends ComponentModelOperations
{
   /**
    * Return the context model manager.
    * @return the context model manager
    */
    ContextManager getContextManager();
    
   /**
    * Return the set of subsidiary component model keys.
    * @return the part keys
    */
    String[] getPartKeys();

   /**
    * Return a subsidiary component manager.
    * @param key the component part key
    * @return the component manager
    * @exception UnknownKeyException if the key is not recognized
    * @see #getPartKeys()
    */
    ComponentManager getComponentManager( String key ) throws UnknownKeyException;

   /**
    * Set the component activation policy to the supplied value.
    * @param policy the new activation policy
    */
    void setActivationPolicy( ActivationPolicy policy );

   /**
    * Override the assigned collection policy.
    * @param policy the collection policy value
    */
    void setCollectionPolicy( CollectionPolicy policy );
}

