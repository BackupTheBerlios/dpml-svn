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

import net.dpml.util.Logger;

/**
 * Example component containing an internal parts defintion.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CompositeComponent
{
   /**
    * Definition of the internal parts.
    */
    public interface Parts
    {
       /**
        * Return the service assigned to the 'widget' key within the 
        * parts of the component.
        * @return the named component
        */
        Widget getWidget();
        
       /**
        * Return the service assigned to the 'gizmo' key within the 
        * parts of the component.
        * @return the named component
        */
        Gizmo getGizmo();
    }
    
    private final Parts m_parts;
    
   /**
    * Creation of a new component using the supplied logger and parts implementation.
    * @param logger a container supplied logging channel
    * @param parts a container supplied implementation of the parts interface
    */
    public CompositeComponent( Logger logger, Parts parts )
    {
        logger.info( "instantiated" );
        m_parts = parts;
    }
    
   /**
    * Return the parts implementation for the testcase.
    * @return the part implementation
    */
    public Parts getParts()
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
