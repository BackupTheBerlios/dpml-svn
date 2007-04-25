/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.runtime;

/**
 * Component interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Component
{
   /**
    * Return the component name.
    * @return the name
    */
    String getName();
    
   /**
    * Add a listener to the component.
    * @param listener the component listener
    */
    void addComponentListener( ComponentListener listener );
    
   /**
    * Remove a listener from the component.
    * @param listener the component listener
    */
    void removeComponentListener( ComponentListener listener );
    
   /**
    * Return a decommissioning provider.
    * @return the provider
    */
    Provider getProvider();
    
   /**
    * Release a provider.
    * @param provider the provider to release
    */
    void release( Provider provider );
    
   /**
    * Terminate the component.
    */
    void terminate();
}

