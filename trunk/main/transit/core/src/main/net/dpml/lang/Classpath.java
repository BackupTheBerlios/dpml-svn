/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.lang;

import java.net.URI;
import java.util.Arrays;

/**
 * A Plugin class contains immutable data about a plugin based on a descriptor resolved
 * from a 'plugin' artifact.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Classpath extends AbstractDirective
{
    private static final URI[] EMPTY = new URI[0];
    
    private final URI[] m_system;
    private final URI[] m_public;
    private final URI[] m_protected;
    private final URI[] m_private;
    private final Classpath m_base;
    
   /**
    * Creation of a empty classpath definition.
    */
    public Classpath()
    {
        this( EMPTY, EMPTY, EMPTY, EMPTY );
    }
    
   /**
    * Creation of a new classpath definition.
    * @param base the base classpath from which this classpath is derived
    * @param extension the extended classpath definition
    */
    public Classpath( final Classpath base, final Classpath extension )
    {
        if( null == extension )
        {
            throw new NullPointerException( "extension" );
        }
        m_base = base;
        m_system = extension.m_system;
        m_public = extension.m_public;
        m_protected = extension.m_protected;
        m_private = extension.m_private;
    }
    
   /**
    * Creation of a new classpath definition.
    * @param systemUris an array of uris representing the system classpath extensions
    * @param publicUris an array of uris representing the public classpath entries
    * @param protectedUris an array of uris representing protected classpath entries
    * @param privateUris an array of uris representing private classpath entries
    */
    public Classpath( 
      URI[] systemUris, URI[] publicUris, URI[] protectedUris, URI[] privateUris )
    {
        this( null, systemUris, publicUris, protectedUris, privateUris );
    }
    
   /**
    * Creation of a new classpath definition.
    * @param base super classpath (may be null)
    * @param systemUris an array of uris representing the system classpath extensions
    * @param publicUris an array of uris representing the public classpath entries
    * @param protectedUris an array of uris representing protected classpath entries
    * @param privateUris an array of uris representing private classpath entries
    */
    public Classpath( 
      Classpath base, URI[] systemUris, URI[] publicUris, URI[] protectedUris, URI[] privateUris )
    {
        m_base = base;
        if( null == systemUris )
        {
            m_system = EMPTY_URIS;
        }
        else
        {
            m_system = systemUris;
        }
        
        if( null == publicUris )
        {
            m_public = EMPTY_URIS;
        }
        else
        {
            m_public = publicUris;
        }
        
        if( null == protectedUris )
        {
            m_protected = EMPTY_URIS;
        }
        else
        {
            m_protected = protectedUris;
        }
        
        if( null == privateUris )
        {
            m_private = EMPTY_URIS;
        }
        else
        {
            m_private = privateUris;
        }
    }
    
   /**
    * Return the base classpath.
    * @return the base classpath that this classpath extends (possibly null)
    */
    public Classpath getBaseClasspath()
    {
        return m_base;
    }
    
   /**
    * Return the classloader dependencies relative to a supplied classloader category.
    *
    * @param category the classloader category
    * @return an array of uris defining the classloader classpath for the supplied category
    */
    public URI[] getDependencies( final Category category )
    {
        if( Category.SYSTEM.equals( category ) )
        {
            return m_system;
        }
        else if( Category.PUBLIC.equals( category ) )
        {
            return m_public;
        }
        else if( Category.PROTECTED.equals( category ) )
        {
            return m_protected;
        }
        else if( Category.PRIVATE.equals( category ) )
        {
            return m_private;
        }
        else
        {
            final String error = 
              "Category not recognized."
              + "\nCategory: " + category;
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return TRUE if equal else FALSE
    */
    public boolean equals( Object other )
    {
        if( other instanceof Classpath )
        {
            Classpath classpath = (Classpath) other;
            //if( equals( m_base, classpath.m_base ) )
            //{
            //    return false;
            //}
            if( !Arrays.equals( m_system, classpath.getDependencies( Category.SYSTEM ) ) )
            {
                return false;
            }
            if( !Arrays.equals( m_public, classpath.getDependencies( Category.PUBLIC ) ) )
            {
                return false;
            }
            if( !Arrays.equals( m_protected, classpath.getDependencies( Category.PROTECTED ) ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_private, classpath.getDependencies( Category.PRIVATE ) );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Return the hashcode for the plugin definition.
    * @return the hash code
    */
    public int hashCode()
    {
        int hash = hashArray( m_system );
        hash ^= hashArray( m_public );
        hash ^= hashArray( m_protected );
        hash ^= hashArray( m_private );
        return hash;
    }
    
   /**
    * Return the classpath as a string.
    * @return the string value
    */
    public String toString()
    {
        return "classpath: [" 
          + m_system.length 
          + ", " + m_public.length 
          + ", " + m_protected.length 
          + ", " + m_private.length
          + "]";
    }
    
    private static final URI[] EMPTY_URIS = new URI[0];
    
}
