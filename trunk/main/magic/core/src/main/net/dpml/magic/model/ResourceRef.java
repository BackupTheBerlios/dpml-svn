/*
 * Copyright 2004 Apache Software Foundation
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

package net.dpml.magic.model;

/**
 * Delcaration of a repository resource reference.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public final class ResourceRef
{
    public static final int ANY = -1;
    public static final int API = 0;
    public static final int SPI = 1;
    public static final int IMPL = 2;

    public static final String LINK = "link";
    public static final String PART = "part";

    private String m_key;
    private Policy m_policy;
    private int m_tag;
    private String m_scope;

    public static int getCategory( final String category )
    {
        if( "api".equals( category ) )
        {
            return API;
        }
        else if( "spi".equals( category ) )
        {
            return SPI;
        }
        else if( "impl".equals( category ) )
        {
            return IMPL;
        }
        else
        {
            return IMPL;
        }
    }

    public static String getCategoryName( final int category )
    {
        if( category == API )
        {
            return "api";
        }
        else if( category == SPI )
        {
            return "spi";
        }
        else
        {
            return "impl";
        }
    }

    public ResourceRef( final String key )
    {
        this( key, new Policy(), ANY );
    }

    public ResourceRef( final String key, final Policy policy, final int tag )
    {
        this( key, policy, tag, LINK );
    }

    public ResourceRef( final String key, final Policy policy, final int tag, String scope )
    {
        m_key = key;
        m_policy = policy;
        m_tag = tag;
        if( null == scope )
        {
            m_scope = LINK ;
        }
        else if( "".equals( scope ) )
        {
            m_scope = LINK ;
        }
        else
        {
            if( LINK.equals( scope ) || PART.equals( scope ))
            {
                m_scope = scope;
            }
            else
            {
                final String error =
                  "Illegal scope attribute value [" + scope
                  + "] in include declaration for the ["
                  + key
                  + "].";
                throw new IllegalArgumentException( error );
            }
        }
    }

    public String getKey()
    {
        return m_key;
    }

    public int getTag()
    {
        return m_tag;
    }

    public Policy getPolicy()
    {
        return m_policy;
    }

    public String getScope()
    {
        return m_scope;
    }

    public boolean matches( final int category )
    {
        if(( ANY == category ) || ( ANY == m_tag ))
        {
            return true;
        }
        else
        {
            return ( m_tag == category );
        }
    }

    public boolean equals( final Object other )
    {
        if( ! ( other instanceof ResourceRef ) )
            return false;

        ResourceRef ref = (ResourceRef) other;

        if( !m_key.equals( ref.m_key ) )
            return false;
        if( !m_scope.equals( ref.m_scope ) )
            return false;
        if( !m_policy.equals( ref.m_policy ) )
            return false;
        if( m_tag != ref.m_tag )
            return false;

        return true;
    }

    public int hashCode()
    {
        int hash = 926234653;
        hash = hash ^ m_key.hashCode();
        hash = hash ^ m_scope.hashCode();
        hash = hash ^ m_policy.hashCode();
        hash = hash ^ ( 1876872534 >> m_tag );
        return hash;
    }

    public String toString()
    {
        return "[resource key=\"" + getKey() + "\", " + m_policy + "]";
    }
}
