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

/**
 * Description of classloader.
 *
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ClassLoaderDirective implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private static final ClasspathDirective[] EMPTY_CLASSPATH_ARRAY = new ClasspathDirective[0]; 

    /**
     * The classpath directives
     */
    private final ClasspathDirective[] m_classpaths;

    /**
     * Create a empty ClasspathDirective.
     */
    public ClassLoaderDirective()
    {
        this( EMPTY_CLASSPATH_ARRAY );
    }

    /**
     * Create a ClassLoaderDirective instance.
     *
     * @param classpaths an array of classpath directives
     */
    public ClassLoaderDirective( final ClasspathDirective[] classpaths )
    {
        if( classpaths == null )
        {
            m_classpaths = EMPTY_CLASSPATH_ARRAY;
        }
        else
        {
            for( int i=0; i<classpaths.length; i++ )
            {
                ClasspathDirective d = classpaths[i];
                if( null == d )
                {
                    throw new NullPointerException( "classpaths (" + i + ")" );
                }
            }
            m_classpaths = classpaths;
        }
    }

    /**
     * Return the set of classpath directives.
     *
     * @return the classpath directive array
     */
    public ClasspathDirective[] getClasspathDirectives()
    {
        return m_classpaths;
    }

   /**
    * Test this object for equality with the supplied object.
    * @param other the other object
    * @return true if the objects are equal
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof ClassLoaderDirective )
            {
                ClassLoaderDirective directive = (ClassLoaderDirective) other;
                if( getClasspathDirectives().length != directive.getClasspathDirectives().length )
                {
                    return false;
                }
                else
                {
                    ClasspathDirective[] mine = getClasspathDirectives();
                    ClasspathDirective[] yours = directive.getClasspathDirectives();
                    for( int i=0; i<mine.length; i++ )
                    {
                        ClasspathDirective m = mine[i];
                        ClasspathDirective n = yours[i];
                        if( !m.equals( n ) )
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
        int hash = 633987582;
        for( int i=0; i<m_classpaths.length; i++ )
        {
            hash ^= m_classpaths[i].hashCode();
        }
        return hash;
    }
}
