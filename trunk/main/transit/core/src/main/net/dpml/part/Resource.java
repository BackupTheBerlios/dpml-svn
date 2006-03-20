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

package net.dpml.part;

import java.io.Serializable;

/**
 * Resource part strategy implementation datatype.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Resource implements Serializable
{
    private final String m_urn;
    private final String m_path;
    
   /**
    * Creation of resource datatype.
    * @param urn the resource urn
    * @param path the resource path
    */
    public Resource( String urn, String path )
    {
        if( null == urn )
        {
            throw new NullPointerException( "urn" );
        }
        if( null == path )
        {
            throw new NullPointerException( "path" );
        }
        m_urn = urn;
        m_path = path;
    }
    
   /**
    * Get the resource urn.
    * @return the urn
    */ 
    public String getURN()
    {
        return m_urn;
    }
    
   /**
    * Get the resource path.
    * @return the path
    */ 
    public String getPath()
    {
        return m_path;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Resource )
        {
            Resource resource = (Resource) other;
            if( !m_urn.equals( resource.m_urn ) )
            {
                return false;
            }
            else
            {
                return m_path.equals( resource.m_path );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_urn.hashCode();
        hash ^= m_path.hashCode();
        return hash;
    }
}