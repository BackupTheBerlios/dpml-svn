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

import java.util.Arrays;

/**
 * Description of the proxy configuration of a Transit system.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProxyDirective extends AbstractDirective
{
    private final String m_host;
    private final String m_username;
    private final char[] m_password;
    private final String[] m_excludes;

   /**
    * Create a new value descriptor using the default java.lang.String class as the base type.
    * @param host the proxy host
    * @param excludes an array of excluded hosts or null if no excludes
    * @param username a possibly null username
    * @param password a possibly null password
    * @exception if the proxy host value is null
    */
    public ProxyDirective( String host, String[] excludes, String username, char[] password )
      throws NullPointerException
    {
        if( null == host )
        {
            throw new NullPointerException( "host" );
        }
        
        m_host = host;
        m_excludes = resolveExcludes( excludes );
        m_username = username;
        m_password = password;
    }
    
   /**
    * Return the proxy host.
    * @return the poxy host
    */
    public String getHost()
    {
        return m_host;
    }

   /**
    * Return the proxy exludes.
    * @return the poxy excludes
    */
    public String[] getExcludes()
    {
        return m_excludes;
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
    * Compare this instance with a supplied object for equality.
    * @param other the other object
    * @return true if the supplied instance is equal to this instance
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProxyDirective ) )
        {
            ProxyDirective directive = (ProxyDirective) other;
            if( !equals( m_host, directive.m_host ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_excludes, directive.m_excludes ) )
            {
                return false;
            }
            else if( !equals( m_username, directive.m_username ) )
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
        int hash = 0;
        hash ^= hashValue( m_host );
        hash ^= hashArray( m_excludes );
        if( null != m_password )
        {
            hash ^= hashValue( m_username );
        }
        if( null != m_password )
        {
            hash ^= new String( m_password ).hashCode();
        }
        return hash;
    }

    private String[] resolveExcludes( String[] excludes )
    {
        if( null == excludes )
        {
            return new String[0];
        }
        else
        {
            return excludes;
        }
    }
    
}
