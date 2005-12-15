/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.part;

import java.io.Serializable;
import java.net.URI;
import java.util.Properties;

/**
 * The generic part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Part extends PartHeader
{
   /**
    * The constant part type identifier.
    */
    static final String ARTIFACT_TYPE = "part";
    
    private final Directive m_directive;
    
   /**
    * Creation of a new Part.
    * @param uri the part controller uri
    * @param properties part properties
    * @param directive the controller specific part directive datastructure
    */
    public Part( URI uri, Properties properties, Directive directive )
    {
        super( uri, properties );
        m_directive = directive;
    }
    
   /**
    * Return the part directive.
    * 
    * @return the part directive
    */
    public Directive getDirective()
    {
        return m_directive;
    }
    
   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else if( !( other instanceof Part ) )
        {
            return false;
        }
        Part part = (Part) other;
        if( null == m_directive )
        {
            return ( null == part.m_directive );
        }
        else
        {
            return m_directive.equals( part.m_directive );
        }
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        if( null != m_directive )
        {
            hash ^= m_directive.hashCode();
        }
        return hash;
    }
    
   /**
    * Return a string representation of the part.
    * @return the string value
    */
    public String toString()
    {
        return "[part controller=" 
          + getControllerURI() 
          + " directive=" 
          + m_directive
          + "]";
    }
}

