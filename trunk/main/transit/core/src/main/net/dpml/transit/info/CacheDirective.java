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
 * Description of the Transit cache configuration.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CacheDirective extends AbstractDirective
{
    public static final String CACHE_PATH = "${dpml.data}/cache";
    public static final String LOCAL_PATH = "file:${dpml.system}/local";
    public static final String LAYOUT = "classic";
    public static final LayoutDirective[] EMPTY_LAYOUTS = new LayoutDirective[0];
    public static final HostDirective[] EMPTY_HOSTS = new HostDirective[0];
    public static final ContentDirective[] EMPTY_CONTENT = new ContentDirective[0];
    
    private final String m_cache;
    private final String m_layout;
    private final String m_local;
    private final LayoutDirective[] m_layouts;
    private final HostDirective[] m_hosts;
    private final ContentDirective[] m_handlers;
    
   /**
    * Create a new CacheDirective.
    */
    public CacheDirective()
    {
        this( 
          CACHE_PATH, LOCAL_PATH, LAYOUT, EMPTY_LAYOUTS, EMPTY_HOSTS, EMPTY_CONTENT );
    }
    
   /**
    * Create a new CacheDirective.
    * @param cache the cache directory path
    * @param local the local repository path
    * @param layout the cache layout strategy
    * @param layouts an array of extended layout descriptors
    * @param hosts an array of supplimentary host descriptors
    * @param handlers an array of custom content handler descriptors
    * @exception NullPointerException if the cache, local, or layout argument is null
    */
    public CacheDirective( 
      String cache, String local, String layout, 
      LayoutDirective[] layouts, HostDirective[] hosts, ContentDirective[] handlers )
      throws NullPointerException
    {
        if( null == cache )
        {
            throw new NullPointerException( "cache" );
        }
        if( null == local )
        {
            throw new NullPointerException( "local" );
        }
        if( null == layout )
        {
            throw new NullPointerException( "layout" );
        }
        
        m_cache = cache;
        m_local = local;
        m_layout = layout;

        if( null == layouts )
        {
            m_layouts = new LayoutDirective[0];
        }
        else
        {
            m_layouts = layouts;
        }
        
        if( null == hosts )
        {
            m_hosts = new HostDirective[0];
        }
        else
        {
            m_hosts = hosts;
        }
        
        if( null == handlers )
        {
            m_handlers = new ContentDirective[0];
        }
        else
        {
            m_handlers = handlers;
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
    public String getLayout()
    {
        return m_layout;
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
    * Return the supplimentary layout plugin configurations.
    *
    * @return the layout directives
    */
    public LayoutDirective[] getLayoutDirectives()
    {
        return m_layouts;
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
    * Return the content handler plugin configurations.
    *
    * @return the content handler directives
    */
    public ContentDirective[] getContentDirectives()
    {
        return m_handlers;
    }
    

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to evaluate
    * @return true if this object is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof CacheDirective ) )
        {
            CacheDirective directive = (CacheDirective) other;
            if( !equals( m_cache, directive.m_cache ) )
            {
                return false;
            }
            else if( !equals( m_local, directive.m_local ) )
            {
                return false;
            }
            else if( !equals( m_layout, directive.m_layout ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_layouts, directive.m_layouts ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_hosts, directive.m_hosts ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_handlers, directive.m_handlers );
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
        hash ^= hashValue( m_cache );
        hash ^= hashValue( m_local );
        hash ^= hashValue( m_layout );
        hash ^= hashArray( m_layouts );
        hash ^= hashArray( m_hosts );
        hash ^= hashArray( m_handlers );
        return hash;
    }
}
