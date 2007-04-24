/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.transit.info;

import java.util.Arrays;
import java.io.Serializable;

import dpml.util.ObjectUtils;

/**
 * Description of the Transit cache configuration.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CacheDirective implements Serializable
{
   /**
    * Default cache path.
    */
    public static final String CACHE_PATH = "${dpml.data}/cache";
    
   /**
    * Default system local repository path.
    */
    public static final String LOCAL_PATH = "file:${dpml.system}/local";
    
   /**
    * Default layout strategy key.
    */
    public static final String CACHE_LAYOUT = "classic";
    
   /**
    * Default layout strategy key.
    */
    public static final String LOCAL_LAYOUT = "modern";
    
   /**
    * Empty resource host array.
    */
    public static final HostDirective[] EMPTY_HOSTS = new HostDirective[0];
    
    private final String m_cache;
    private final String m_cacheLayout;
    private final String m_local;
    private final String m_localLayout;
    private final HostDirective[] m_hosts;
    
   /**
    * Create a new CacheDirective.
    */
    public CacheDirective()
    {
        this( 
          CACHE_PATH, CACHE_LAYOUT, LOCAL_PATH, LOCAL_LAYOUT, 
          EMPTY_HOSTS );
    }
    
   /**
    * Create a new CacheDirective.
    * @param hosts the assigned remmote hosts
    */
    public CacheDirective( HostDirective[] hosts )
    {
        this( 
          CACHE_PATH, CACHE_LAYOUT, LOCAL_PATH, LOCAL_LAYOUT, 
          hosts );
    }
    
   /**
    * Create a new CacheDirective.
    * @param cache the cache directory path
    * @param cacheLayout the cache layout strategy
    * @param local the local repository path
    * @param localLayout the local repository layout strategy
    * @param hosts an array of supplimentary host directives
    * @exception NullPointerException if the cache, local, or layout argument is null
    */
    public CacheDirective( 
      String cache, String cacheLayout, String local, String localLayout, 
      HostDirective[] hosts )
      throws NullPointerException
    {
        if( null == cache )
        {
            throw new NullPointerException( "cache" );
        }
        if( null == cacheLayout )
        {
            throw new NullPointerException( "cacheLayout" );
        }
        if( null == local )
        {
            throw new NullPointerException( "local" );
        }
        if( null == localLayout )
        {
            throw new NullPointerException( "localLayout" );
        }
        
        m_cache = cache;
        m_cacheLayout = cacheLayout;
        m_local = local;
        m_localLayout = localLayout;
        
        if( null == hosts )
        {
            m_hosts = new HostDirective[0];
        }
        else
        {
            m_hosts = hosts;
        }
    }
    
   /**
    * Return the cache path.
    *
    * @return the cache path
    */
    public String getCache()
    {
        return m_cache;
    }
    
   /**
    * Return the cache layout id.
    *
    * @return the cache layout identifier
    */
    public String getCacheLayout()
    {
        return m_cacheLayout;
    }
    
   /**
    * Return the local repository path.
    *
    * @return the local repository path
    */
    public String getLocal()
    {
        return m_local;
    }
    
   /**
    * Return the local system repository layout id.
    *
    * @return the system layout identifier
    */
    public String getLocalLayout()
    {
        return m_localLayout;
    }
    
   /**
    * Return the supplimentary layout plugin configurations.
    *
    * @return the host directives
    */
    public HostDirective[] getHostDirectives()
    {
        return m_hosts;
    }
    
   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to evaluate
    * @return true if this object is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( other instanceof CacheDirective )
        {
            CacheDirective directive = (CacheDirective) other;
            if( !ObjectUtils.equals( m_cache, directive.m_cache ) )
            {
                return false;
            }
            else if( !ObjectUtils.equals( m_cacheLayout, directive.m_cacheLayout ) )
            {
                return false;
            }
            else if( !ObjectUtils.equals( m_local, directive.m_local ) )
            {
                return false;
            }
            else if( !ObjectUtils.equals( m_localLayout, directive.m_localLayout ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_hosts, directive.m_hosts );
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
        int hash = getClass().hashCode();
        hash ^= ObjectUtils.hashValue( m_cache );
        hash ^= ObjectUtils.hashValue( m_cacheLayout );
        hash ^= ObjectUtils.hashValue( m_local );
        hash ^= ObjectUtils.hashValue( m_localLayout );
        hash ^= ObjectUtils.hashArray( m_hosts );
        return hash;
    }
}
