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
 * 
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PartHolder implements Serializable
{
    private final URI m_handler;
    private final byte[] m_part;

    public PartHolder( URI handler, byte[] part )
    {
        m_handler = handler;
        m_part = part;
    }

    public URI getPartHandlerURI()
    {
        return m_handler;
    }

    public byte[] getByteArray()
    {
        return m_part;
    }

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

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof PartHolder )
        {
            PartHolder holder = (PartHolder) other;
            if( getPartHandlerURI().equals( holder.getPartHandlerURI() ) == false )
            {
                return false;
            }
            else if( Arrays.equals( getByteArray(), holder.getByteArray() ) == false )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return "[part-holder handler:" + m_handler + " size:" + m_part.length + "]";
    }

}

