/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data;

import java.io.Serializable;
import java.net.URI;

import net.dpml.transit.Category;

/**
 * Description of classpath.
 *
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ClasspathDirective implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private static final URI[] EMPTY_URIS = new URI[0]; 

    /**
     * The resource references
     */
    private final URI[] m_uris;

    /**
     * The resource references
     */
    private final Category m_category;

    /**
     * Create a ClasspathDirective instance.
     *
     * @param uris the set of artifact uris
     */
    public ClasspathDirective( final Category category, final URI[] uris )
    {
        m_category = category;
        if( uris == null )
        {
            m_uris = EMPTY_URIS;
        }
        else
        {
            m_uris = uris;
        }
    }

   /**
    * Return the category name.  
    * @return the category name
    */
    public Category getCategory()
    {
        return m_category;
    }

   /**
    * Return the default status of this directive.  If TRUE
    * the enclosed repository and fileset directives are empty.
    */
    public boolean isEmpty()
    {
        final int n = m_uris.length;
        return n == 0;
    }

    /**
     * Return the set of artifact directives.
     *
     * @return the artifact directive set
     */
    public URI[] getURIs()
    {
        return m_uris;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof ClasspathDirective )
            {
                ClasspathDirective directive = (ClasspathDirective) other;
                if( !getCategory().equals( directive.getCategory() ) )
                {
                    return false;
                }
                else if( getURIs().length != directive.getURIs().length )
                {
                    return false;
                }
                else
                {
                    URI[] myUris = getURIs();
                    URI[] yourURIs = directive.getURIs();
                    for( int i=0; i<myUris.length; i++ )
                    {
                        URI m = myUris [i];
                        URI n = yourURIs[i];
                        if( false == m.equals( n ) )
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = m_category.hashCode();
        for( int i=0; i<m_uris.length; i++ )
        {
            hash ^= m_uris[i].hashCode();
        }
        return hash;
    }

}
