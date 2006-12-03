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

package net.dpml.transit.info;

import net.dpml.lang.AbstractDirective;

/**
 * Description of a host configuration within Transit.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HostDirective extends AbstractDirective
{
    private final String m_id;
    private final int m_priority;
    private final String m_host;
    private final String m_index;
    private final String m_username;
    private final char[] m_password;
    private final boolean m_enabled;
    private final boolean m_trusted;
    private final String m_layout;
    private final String m_scheme;
    private final String m_prompt;

   /**
    * Creation of a new host description.
    * @param id a unique resource host identifier
    * @param priority the host prority
    * @param host the resource host
    * @param index the name of an index resource (may be null)
    * @param username a possibly null username
    * @param password a possibly null password
    * @param enabled true if enabled
    * @param trusted true if trusted
    * @param layout the name of the layout strategy
    * @param scheme the security scheme (may be null)
    * @param prompt authentication prompt (may be null)
    * @exception NullPointerException if the id, host, or layout are null
    */
    public HostDirective( 
      String id, int priority, String host, String index, String username, char[] password, boolean enabled, 
      boolean trusted, String layout, String scheme, String prompt )
      throws NullPointerException
    {
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        if( null == host )
        {
            throw new NullPointerException( "host" );
        }
        if( null == layout )
        {
            throw new NullPointerException( "layout" );
        }
        
        m_id = id;
        m_priority = priority;
        m_host = host;
        m_index = index;
        m_username = username;
        m_password = password;
        m_enabled = enabled;
        m_trusted = trusted;
        m_layout = layout;
        
        if( null == scheme )
        {
            m_scheme = "";
        }
        else
        {
            m_scheme = scheme;
        }
        
        if( null == prompt )
        {
            m_prompt = "";
        }
        else
        {
            m_prompt = prompt;
        }
        
    }
    
   /**
    * Return the host identifier.
    * @return the host id
    */
    public String getID()
    {
        return m_id;
    }

   /**
    * Return the host priority.
    * @return the host priority value
    */
    public int getPriority()
    {
        return m_priority;
    }

   /**
    * Return the resource host.
    * @return the host
    */
    public String getHost()
    {
        return m_host;
    }

   /**
    * Return the index resource name.
    * @return the host index resource name
    */
    public String getIndex()
    {
        return m_index;
    }

   /**
    * Return the proxy username.
    * @return the poxy username
    */
    public String getUsername()
    {
        return m_username;
    }

   /**
    * Return the proxy password.
    * @return the poxy password
    */
    public char[] getPassword()
    {
        return m_password;
    }

   /**
    * Return the enabled status.
    * @return true if enabled
    */
    public boolean getEnabled()
    {
        return m_enabled;
    }

   /**
    * Return the trusted status.
    * @return true if trusted
    */
    public boolean getTrusted()
    {
        return m_trusted;
    }

   /**
    * Return the layout name.
    * @return the layout key
    */
    public String getLayout()
    {
        return m_layout;
    }

   /**
    * Return the authentication prompt.
    * @return the layout key
    */
    public String getPrompt()
    {
        return m_prompt;
    }
    
   /**
    * Return the authentication scheme.
    * @return the scheme
    */
    public String getScheme()
    {
        return m_scheme;
    }
    
   /**
    * Compare this instance with a supplied object for equality.
    * @param other the other object
    * @return true if the supplied instance is equal to this instance
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof HostDirective ) )
        {
            HostDirective directive = (HostDirective) other;
            if( !equals( m_id, directive.m_id ) )
            {
                return false;
            }
            else if( m_priority != directive.m_priority )
            {
                return false;
            }
            else if( m_trusted != directive.m_trusted )
            {
                return false;
            }
            else if( m_enabled != directive.m_enabled )
            {
                return false;
            }
            else if( !equals( m_host, directive.m_host ) )
            {
                return false;
            }
            else if( !equals( m_index, directive.m_index ) )
            {
                return false;
            }
            else if( !equals( m_username, directive.m_username ) )
            {
                return false;
            }
            else if( !equals( m_layout, directive.m_layout ) )
            {
                return false;
            }
            else if( !equals( m_scheme, directive.m_scheme ) )
            {
                return false;
            }
            else if( !equals( m_prompt, directive.m_prompt ) )
            {
                return false;
            }
            else
            {
                if( null == m_password )
                {
                    return null == directive.m_password;
                }
                else
                {
                    if( null == directive.m_password )
                    {
                        return false;
                    }
                    else
                    {
                        return new String( m_password ).equals( new String( directive.m_password ) );
                    }
                }
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the instance hashcode value.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = m_priority;
        hash ^= hashValue( m_id );
        hash ^= hashValue( new Boolean( m_trusted ) );
        hash ^= hashValue( new Boolean( m_enabled ) );
        hash ^= hashValue( m_host );
        hash ^= hashValue( m_index );
        hash ^= hashValue( m_username );
        if( null != m_password )
        {
            hash ^= new String( m_password ).hashCode();
        }
        hash ^= hashValue( m_layout );
        hash ^= hashValue( m_scheme );
        hash ^= hashValue( m_prompt );
        return hash;
    }

}
