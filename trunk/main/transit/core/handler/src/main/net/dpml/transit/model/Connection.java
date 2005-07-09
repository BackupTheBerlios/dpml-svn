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

package net.dpml.transit.model;

import java.io.Serializable;
import java.rmi.registry.Registry;

/**
 * The Connection class describes a service access point used during 
 * publication or location of a service.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Connection implements Serializable
{
    private String m_host;
    private int m_port;
    private boolean m_optional;
    private boolean m_enabled;

   /**
    * Creation of an optional enabled connection with a null host reference and 
    * port value of 1099.
    */
    public Connection()
    {
       this( null, Registry.REGISTRY_PORT, true, true );
    }

   /**
    * Creation of a connection description with a supplied
    * host and port.
    * @param host the connection host
    * @param port the port
    * @param optional the optional status
    * @param enabled the enabled status
    */
    public Connection( String host, int port, boolean optional, boolean enabled )
    {
        m_host = host;
        m_port = port;
        m_optional = optional;
        m_enabled = enabled;
    }

   /**
    * Return the connection host.
    * @return the host value
    */
    public String getHost()
    {
        return m_host;
    }

   /**
    * Return the connection port.
    * @return the port value
    */
    public int getPort()
    {
        return m_port;
    }

   /**
    * Return TRUE if this connection description is enabled.
    * @return the enabled status
    */
    public boolean isEnabled()
    {
        return m_enabled;
    }

   /**
    * Return TRUE if this connection is optional.
    * @return the optional status
    */
    public boolean isOptional()
    {
        return m_optional;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( false == ( other instanceof Connection ) )
        {
            return false;
        }
        else
        {
            Connection connection = (Connection) other;
            if( ( null == m_host ) && ( null != connection.m_host ) )
            {
                return false;
            }
            else if( m_port != connection.m_port )
            {
                return false;
            }
            else if( m_optional != connection.m_optional )
            {
                return false;
            }
            else if( m_enabled != connection.m_enabled )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public int hashCode()
    {
        int hash = new Integer( m_port ).hashCode();
        if( null != m_host )
        {
            hash ^= m_host.hashCode();
        }
        hash ^= new Boolean( m_optional ).hashCode();
        hash ^= new Boolean( m_enabled ).hashCode();
        return hash;
    }
}

