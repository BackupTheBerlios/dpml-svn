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

/**
 * Description of classloader.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ClassLoaderDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ClassLoaderDirective implements Serializable
{
    private static final LibraryDirective EMPTY_LIBRARY = new LibraryDirective();
    private static final ClasspathDirective EMPTY_CLASSPATH = new ClasspathDirective();

    /**
     * The library directive.
     */
    private LibraryDirective m_library;

    /**
     * The root category hierachy.
     */
    private ClasspathDirective m_classpath;
    
    /**
     * Create an empty ClassloaderDirective.
     */
    public ClassLoaderDirective()
    {
        this( null, null );
    }

    /**
     * Create a ClassloaderDirective instance.
     *
     * @param library the library descriptor
     * @param classpath the classpath descriptor
     */
    public ClassLoaderDirective( 
       final LibraryDirective library,
       final ClasspathDirective classpath )
    {
        if( library == null )
        {
            m_library = EMPTY_LIBRARY;
        }
        else
        {
            m_library = library;
        }

        if( classpath == null )
        {
            m_classpath = EMPTY_CLASSPATH;
        }
        else
        {
            m_classpath = classpath;
        }
    }

   /**
    * Return true if the library and classpath declarations are empty.
    * If the function returns true, this directive is in an effective 
    * default state and need not be externalized.
    *
    * @return the empty status of this directive
    */
    public boolean isEmpty()
    {
        return ( m_library.isEmpty() && m_classpath.isEmpty() );
    }

    /**
     * Return the library directive.
     *
     * @return the library directive.
     */
    public LibraryDirective getLibrary()
    {
        return m_library;
    }

    /**
     * Return the classpath directive.
     *
     * @return the classpath directive.
     */
    public ClasspathDirective getClasspathDirective()
    {
        return m_classpath;
    }
}
