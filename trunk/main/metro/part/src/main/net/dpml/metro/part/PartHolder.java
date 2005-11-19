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

package net.dpml.metro.part;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;

/**
 * The PartHolder class is responsible for the association of a part handler
 * uri with a part datatype.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartHolder implements Serializable
{
    private final URI m_handler;
    private final byte[] m_part;

   /**
    * Creation of a new part holder.
    * @param handler the controller uri
    * @param part a byte array representing the part datastructure
    */
    public PartHolder( URI handler, byte[] part )
    {
        m_handler = handler;
        m_part = part;
    }

   /**
    * Return the part controller uri.
    * @return the uri of the controller 
    */
    public URI getPartHandlerURI()
    {
        return m_handler;
    }
  
   /**
    * Return the part datastructure as a byte array.
    * @return the byte array
    */
    public byte[] getByteArray()
    {
        return m_part;
    }

   /**
    * Return the part holder hash code.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_handler.hashCode();
        for( int i=0; i<m_part.length; i++ )
        {
            hash ^= new Byte( m_part[i] ).hashCode();
        }
        return hash;
    }

   /**
    * Test this object for equality with the supplied object.
    * @param other the other object
    * @return true iof the objects are equal
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof PartHolder )
        {
            PartHolder holder = (PartHolder) other;
            if( !getPartHandlerURI().equals( holder.getPartHandlerURI() ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( getByteArray(), holder.getByteArray() );
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Return a string representation of this part holder.
    * @return the string value
    */
    public String toString()
    {
        return "[part-holder handler:" + m_handler + " size:" + m_part.length + "]";
    }

}

