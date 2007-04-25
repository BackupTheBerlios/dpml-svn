/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package org.acme;

import net.dpml.annotation.Parts;

import net.dpml.runtime.Component;
import net.dpml.runtime.Provider;

/**
 * Sample component that uses an explicit custom parts interface using the 
 * Parts annotation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ControllerComponent
{
   /**
    * A parts interface asserted using the parts annotation.
    */
    @Parts
    public interface ControllerParts
    {
       /**
        * Get a subcomponent by name.
        * @return the component named widget.
        */
        Component getWidget();
        
       /**
        * Get a subcomponent by name.
        * @return the component named gizmo.
        */
        Provider getGizmo();
    }
    
    private final ControllerParts m_parts;
    
   /**
    * Creation of the controller test component.
    * @param parts the container provided part implementation
    */
    public ControllerComponent( ControllerParts parts )
    {
        m_parts = parts;
    }
    
   /**
    * Get the parts implementation for the testcase.
    * @return the parts implementation 
    */
    public ControllerParts getParts()
    {
        return m_parts;
    }
    
   /**
    * Test the supplied object for equality with this object.
    * @param other the supplied object 
    * @return the equality result
    */
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }

   /**
    * Get the component hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        return m_parts.hashCode();
    }
}
