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

package net.dpml.composition.info;

import java.net.URI;
import java.util.Arrays;
import java.io.Serializable;

/**
 * 
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class TypeHolder implements Serializable
{
    static final long serialVersionUID = 1L;

    private final URI m_handler;
    private final byte[] m_type;

    public TypeHolder( URI handler, byte[] part )
    {
        m_handler = handler;
        m_type = part;
    }

    public URI getTypeHandlerURI()
    {
        return m_handler;
    }

    public byte[] getByteArray()
    {
        return m_type;
    }

    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_handler.hashCode();
        for( int i=0; i<m_type.length; i++ )
        {
            hash ^= new Byte( m_type[i] ).hashCode();
        }
        return hash;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof TypeHolder )
        {
            TypeHolder holder = (TypeHolder) other;
            if( getTypeHandlerURI().equals( holder.getTypeHandlerURI() ) == false )
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
        return "[type-holder handler:" + m_handler + " size:" + m_type.length + "]";
    }

}

