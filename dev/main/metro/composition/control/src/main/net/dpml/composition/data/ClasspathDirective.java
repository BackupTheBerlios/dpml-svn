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

package net.dpml.composition.data;

import java.io.Serializable;
import java.net.URI;

/**
 * Description of classpath.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ClasspathDirective.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class ClasspathDirective implements Serializable
{
     private static final FilesetDirective[] EMPTY_FILESETS = new FilesetDirective[0]; 
     private static final URI[] EMPTY_URIS = new URI[0]; 

    /**
     * The fileset directives
     */
    private FilesetDirective[] m_filesets;

    /**
     * The resource references
     */
    private URI[] m_uris;

    /**
     * The resource references
     */
    private String m_name;

    /**
     * Create a empty ClasspathDirective.
     */
    public ClasspathDirective()
    {
        this( "?", null );
    }

    /**
     * Create a ClasspathDirective instance.
     *
     * @param uris the set of artifact uris
     */
    public ClasspathDirective( final String name, final URI[] uris )
    {
        this( name, null, uris );
    }

    /**
     * Create a ClasspathDirective instance.
     *
     * @param filesets the filesets to be included in a classloader
     * @param uris the set of artifact uris
     */
    public ClasspathDirective( 
       final String name, 
       final FilesetDirective[] filesets, 
       final URI[] uris )
    {
        m_name = name;
        if( filesets == null )
        {
            m_filesets = EMPTY_FILESETS;
        }
        else
        {
            m_filesets = filesets;
        }

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
    public String getCategoryName()
    {
        return m_name;
    }


   /**
    * Return the default status of this directive.  If TRUE
    * the enclosed repository and fileset directives are empty.
    */
    public boolean isEmpty()
    {
        final int n = m_uris.length + m_filesets.length;
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

    /**
     * Return the set of fileset directives.
     *
     * @return the fileset directives
     */
    public FilesetDirective[] getFilesets()
    {
        return m_filesets;
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
                if( false == getCategoryName().equals( directive.getCategoryName() ) )
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
                }
                if( getFilesets().length != directive.getFilesets().length )
                {
                    return false;
                }
                else
                {
                    FilesetDirective[] mine = getFilesets();
                    FilesetDirective[] yours = directive.getFilesets();
                    for( int i=0; i<mine.length; i++ )
                    {
                        FilesetDirective d = mine[i];
                        FilesetDirective e = yours[i];
                        if( false == d.equals( e ) )
                        {
                            return false;
                        }
                    }
                }
                return true;
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
        int hash = m_name.hashCode();
        for( int i=0; i<m_filesets.length; i++ )
        {
            hash ^= m_filesets[i].hashCode();
        }
        for( int i=0; i<m_uris.length; i++ )
        {
            hash ^= m_uris[i].hashCode();
        }
        return hash;
    }

}
