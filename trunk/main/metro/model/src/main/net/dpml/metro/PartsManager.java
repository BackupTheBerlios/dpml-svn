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

import net.dpml.part.ControlException;

import net.dpml.lang.UnknownKeyException;

/**
 * Local interface through which a component implementation may 
 * interact with subsidary parts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface PartsManager
{
   /**
    * Return the array of keys used to identify internal parts.
    * @return the part key array
    */
    String[] getKeys();
    
   /**
    * Return a local component handler.
    * @param key the internal part key
    * @return the local component handler
    * @exception UnknownKeyException the key is not recognized
    */
    ComponentHandler getComponentHandler( String key ) throws UnknownKeyException;
    
   /**
    * Return the commissioned state of the part collection.
    * @return true if commissioned else false
    */
    boolean isCommissioned();
    
   /**
    * Initiate the oprdered activation of all internal parts.
    * @exception ControlException if an activation error occurs
    */
    void commission() throws ControlException;
    
   /**
    * Initiate deactivation of all internal parts.
    */
    void decommission();
}

