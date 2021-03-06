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

/**
 * Test componnet that leverages generics in the definition of 
 * parts method a return type.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GenericComponent
{
   /**
    * Definition of a parts interface using generics.
    */
    @Parts
    public interface GenericParts
    {
       /**
        * Get a service instance of the component named 'widget' 
        * assigned to the type T.
        * @param type the return type
        * @return the service instance as an instance of T
        */
        <T>T getWidget( Class<T> type );
        
       /**
        * Get a service instance of the component named 'gizmo' 
        * assigned to the type T.
        * @param type the return type
        * @return the service instance as an instance of T
        */
        <T>T getGizmo( Class<T> type );
    }
    
    private final GenericParts m_parts;
    
   /**
    * Generic component constructor.
    * @param parts the container supplied parts implementation
    */
    public GenericComponent( GenericParts parts )
    {
        m_parts = parts;
    }
    
   /**
    * Get the part implementation for the testcase.
    * @return the parts implementation
    */
    public GenericParts getParts()
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
