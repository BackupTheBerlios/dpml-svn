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

package org.apache.avalon.composition.data;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>An library directive.</p>
 * <p><b>XML</b></p>
 * <p>An library element is normally contained within a scoping structure such as a
 * classloader directive. The library element may contain any number of "include" 
 * or "group" elements.</p>
 * <pre>
 *    <font color="gray">&lt;library&gt;</font>
 *       &lt;include&gt;lib&lt;/include&gt;
 *       &lt;group&gt;avalon-framework&lt;/group&gt;
 *    <font color="gray">&lt;/library&gt;</font>
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LibraryDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class LibraryDirective implements Serializable
{
     private static final String[] EMPTY_SET = new String[0];

    /**
     * The include paths
     */
    private final String[] m_includes;

    /**
     * The groups
     */
    private final String[] m_groups;

    /**
     * Create a new LibraryDirective instance.
     */
    public LibraryDirective( )
    {
        this( null, null );
    }

    /**
     * Create a new LibraryDirective instance.
     *
     * @param includes the set of include paths
     * @param groups the set of group identifiers
     */
    public LibraryDirective( final String[] includes, final String[] groups )
    {
        if( includes == null )
        {
            m_includes = EMPTY_SET;
        }
        else
        {
            m_includes = includes;
        }

        if( groups == null )
        {
            m_groups = EMPTY_SET;
        }
        else
        {
            m_groups = groups;
        }
    }

   /**
    * Return the empty status of this directive.
    */
    public boolean isEmpty()
    {
        final int n = m_includes.length + m_groups.length;
        return n == 0;
    }

    /**
     * Return the set of include path entries.
     *
     * @return the include paths
     */
    public String[] getIncludes()
    {
        return m_includes;
    }

    /**
     * Return the set of group identifiers.
     *
     * @return the group identifiers
     */
    public String[] getGroups()
    {
        return m_groups;
    }

    /**
     * Return the set of optional extension locations as a File[] 
     * relative to a supplied base directory.
     *
     * @param base a base directory against which relatve references shall be resolved 
     * @return an array of extension library locations
     * @exception IOException if a path cannot be resolved to a directory
     */
    public File[] getOptionalExtensionDirectories( File base ) throws IOException
    {
        if( base == null )
        {
            throw new NullPointerException( "base" );
        }
        String[] includes = getIncludes();
        ArrayList list = new ArrayList();
        for( int i=0; i<includes.length; i++ )
        {
            final String path = includes[i];
            list.add( getDirectory( base, path ) );
        }
        return (File[]) list.toArray( new File[0] );
    }

    private File getDirectory( File base, String path ) throws IOException
    {
        File file = new File( path );
        if( file.isAbsolute() )
        {
            return verifyDirectory( file );
        }
        return verifyDirectory( new File( base, path ) );
    }

    private File verifyDirectory( File dir ) throws IOException
    {
        if( dir.isDirectory() )
        {
            return dir.getCanonicalFile();
        }

        final String error = 
          "Path does not correspond to a directory: " + dir;
        throw new IOException( error );
    }
}
