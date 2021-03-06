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
 * The CodeBaseDirective is immutable datastructure used to 
 * describe a codebase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitDirective extends AbstractDirective
{
   /**
    * Classic configuration profile.
    */
    public static final TransitDirective CLASSIC_PROFILE = createClassicProfile();
    
    private final ProxyDirective m_proxy;
    private final CacheDirective m_cache;
    
   /**
    * Creation of a new codebase descriptor.
    * @param proxy the proxy configuration
    * @param cache the cache configuration
    * @exception NullPointerException if the cache configuration arguments is null
    */
    public TransitDirective( ProxyDirective proxy, CacheDirective cache )
      throws NullPointerException
    {
        if( null == cache )
        {
            throw new NullPointerException( "cache" );
        }
        
        m_proxy = proxy;
        m_cache = cache;
    }
    
   /**
    * Return the cache configuration directive.
    *
    * @return the cache directive
    */
    public CacheDirective getCacheDirective()
    {
        return m_cache;
    }
    
   /**
    * Return the proxy configuration.
    *
    * @return the proxy directive (possibly null)
    */
    public ProxyDirective getProxyDirective()
    {
        return m_proxy;
    }
    
   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to evaluate
    * @return true if this object is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof TransitDirective ) )
        {
            TransitDirective directive = (TransitDirective) other;
            if( !equals( m_proxy, directive.m_proxy ) )
            {
                return false;
            }
            else 
            {
                return equals( m_cache, directive.m_cache );
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
        int hash = super.hashCode();
        hash ^= hashValue( m_proxy );
        hash ^= hashValue( m_cache );
        return hash;
    }
    
    private static TransitDirective createClassicProfile()
    {
        HostDirective[] hosts = new HostDirective[4];
        hosts[0] = 
          new HostDirective( 
            "dpml", 40, "http://repository.dpml.net/classic", null, null, null, 
            true, false, "classic", null, null );
        hosts[1] = 
          new HostDirective( 
            "ibiblio", 70, "http://www.ibiblio.org/maven", null, null, null, 
            true, false, "classic", null, null );
        hosts[2] = 
          new HostDirective( 
            "m2", 100, "http://www.ibiblio.org/maven2", null, null, null, 
            true, false, "modern", null, null );
        hosts[3] = 
          new HostDirective( 
            "apache", 140, "http://www.apache.org/dist/java-repository", null, null, null,
            true, false, "classic", null, null );
        
        CacheDirective cache = 
          new CacheDirective( 
            CacheDirective.CACHE_PATH,
            CacheDirective.CACHE_LAYOUT,
            CacheDirective.LOCAL_PATH,
            CacheDirective.LOCAL_LAYOUT,
            CacheDirective.EMPTY_LAYOUTS,
            hosts );
        return new TransitDirective( null, cache );
    }
    
}
