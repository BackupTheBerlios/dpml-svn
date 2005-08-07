/*
 * Copyright 2004 Niclas Hedhman, DPML
 * Copyright 2005 Stephen McConnell, DPML
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

package net.dpml.transit.model;

import java.io.Serializable;

import net.dpml.transit.NullArgumentException;

/**
 * A request identifier.
 */
public final class RequestIdentifier implements Serializable
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Used in hashcode calculation.
    */
    private static final int MAGIC_NUMBER = 72349724;

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The internet address.
    */
    private final String m_address;

   /**
    * The port.
    */
    private final int m_port;

   /**
    * The prompt.
    */
    private final String m_prompt;

   /**
    * The protocol.
    */
    private final String m_protocol;

   /**
    * The scheme.
    */
    private final String m_scheme;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new request identifier.
    * @param address the address
    * @param port the port
    * @param protocol the protocol
    * @param scheme the scheme
    * @param prompt the prompt
    * @exception NullArgumentException if any of the address, protocol, scheme
    *   or prompt arguments is null.
    */
    public RequestIdentifier( 
      String address, int port, String protocol, String scheme, String prompt ) 
      throws NullArgumentException
    {
        if( address == null )
        {
            throw new NullArgumentException( "address" );
        }
        if( protocol == null )
        {
            throw new NullArgumentException( "protocol" );
        }
        if( scheme == null )
        {
            throw new NullArgumentException( "scheme" );
        }
        if( prompt == null )
        {
            throw new NullArgumentException( "prompt" );
        }
        m_address = address;
        m_port = port;
        m_protocol = protocol;
        m_prompt = prompt;
        m_scheme = scheme;
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Test for equality.
    * @param obj the object to test against
    * @return true if this object is the same as the supplied object
    */
    public boolean equals( Object obj )
    {
        if( !( obj instanceof RequestIdentifier ) )
        {
            return false;
        }
        RequestIdentifier other = (RequestIdentifier) obj;

        if( m_port != other.m_port )
        {
            return false;
        }

        if( !m_address.equals( other.m_address ) )
        {
            return false;
        }
        if( !m_protocol.equals( other.m_protocol ) )
        {
            return false;
        }
        if( !m_prompt.equals( other.m_prompt ) )
        {
            return false;
        }
        if( !m_scheme.equals( other.m_scheme ) )
        {
            return false;
        }
        return true;
    }

   /**
    * Return the hascode for this instance.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = MAGIC_NUMBER * m_port;
        hash = hash ^ m_address.hashCode();
        hash = hash ^ m_protocol.hashCode();
        hash = hash ^ m_prompt.hashCode();
        hash = hash ^ m_scheme.hashCode();
        return hash;
    }

   /**
    * Return the string representation of this instance.
    * @return the string value
    */
    public String toString()
    {
        StringBuffer b = new StringBuffer();
        b.append( "ID[ " );
        b.append( m_protocol );
        b.append( ", " );
        b.append( m_address );
        b.append( ", " );
        b.append( m_port );
        b.append( ", " );
        b.append( m_scheme );
        b.append( ", " );
        b.append( m_prompt );
        b.append( " ]" );
        return b.toString();
    }
}

