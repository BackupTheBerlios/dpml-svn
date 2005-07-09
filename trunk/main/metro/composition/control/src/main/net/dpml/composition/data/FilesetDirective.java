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

/**
 * <p>A fileset directive is a scoped defintion of a set of files.  A fileset
 * a structurally defined as a base directory and a set of relative filenames
 * represented as include directives and/or exclude directives.</p>
 *
 * <p><b>XML</b></p>
 * <pre>
 *   &lt;fileset dir="<font color="darkred">lib</font>"&gt;
 *     &lt;include name="<font color="darkred">avalon-framework.jar</font>"/&gt;
 *     &lt;include name="<font color="darkred">logkit.jar</font>"/&gt;
 *     &lt;exclude name="<font color="darkred">servlet.jar</font>"/&gt;
 *   &lt;/dirset&gt;
 * </pre>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: FilesetDirective.java 2119 2005-03-23 02:04:46Z mcconnell@dpml.net $
 */
public class FilesetDirective implements Serializable
{
    /**
     * The base directory from which include directives will be resolved.
     */
    private final String m_base;

    /**
     * The set of include directives.
     */
    private final Include[] m_includes;

    /**
     * The set of exclude directives.
     */
    private final Exclude[] m_excludes;

    /**
     * Create a FilesetDirective instance.
     *
     * @param base the base directory path against which includes are evaluated
     * @param includes the set of includes to include in the fileset
     */
    public FilesetDirective( final String base, final Include[] includes )
    {
        this(base, includes, null);
    }

    /**
     * Create a FilesetDirective instance.
     *
     * @param base the base directory path against which includes are evaluated
     * @param includes the set of includes to include in the fileset
     */
    public FilesetDirective( final String base, final Include[] includes,
            final Exclude[] excludes)
    {
        if( null == base )
        {
            throw new NullPointerException( "base" );
        }
        m_base = base;
        m_includes = includes;
        m_excludes = excludes;
    }

    /**
     * Return the base directory.
     *
     * @return the directory
     */
    public String getBaseDirectory()
    {
        return m_base;
    }

    /**
     * Return the set of include directives.
     *
     * @return the include set
     */
    public Include[] getIncludes()
    {
        return m_includes;
    }

    /**
     * Return the set of exclude directives.
     *
     * @return the exclude set
     */
    public Exclude[] getExcludes()
    {
        return m_excludes;
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof FilesetDirective )
            {
                FilesetDirective fileset = (FilesetDirective) other;
                if( false == m_base.equals( fileset.getBaseDirectory() ) )
                {
                    return false;
                }
                if( getIncludes().length != fileset.getIncludes().length )
                {
                    return false;
                }
                else
                {
                    Include[] mine = getIncludes();
                    Include[] yours = fileset.getIncludes();
                    for( int i=0; i<mine.length; i++ )
                    {
                        Include p = mine[i];
                        Include q = yours[i];
                        if( false == p.equals( q ) )
                        {
                            return false;
                        }
                    }
                }
                if( getExcludes().length != fileset.getExcludes().length )
                {
                    return false;
                }
                else
                {
                    Exclude[] mine = getExcludes();
                    Exclude[] yours = fileset.getExcludes();
                    for( int i=0; i<mine.length; i++ )
                    {
                        Exclude p = mine[i];
                        Exclude q = yours[i];
                        if( false == p.equals( q ) )
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
        int hash = m_base.hashCode();
        for( int i=0; i<m_includes.length; i++ )
        {
            hash ^= m_includes[i].hashCode();
        }
        for( int i=0; i<m_excludes.length; i++ )
        {
            hash ^= m_excludes[i].hashCode();
        }
        return hash;
    }


   /**
    * <p>A file exclude directive.</p>
    * <p><b>XML</b></p>
    * <p>An exclude element is normally contained within a scoping structure such as a
    * fileset or directory set. The exclude element contains the single attribute name
    * which is used to refer to the file or directory (depending on the containing
    * context.</p>
    * <pre>
    *    <font color="gray">&lt;fileset dir="lib"&gt;</font>
    *       &lt;exclude name="<font color="darkred">avalon-framework.jar</font>" /&gt;
    *    <font color="gray">&lt;/fileset&gt;</font>
    * </pre>
    *
    * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
    * @version $Id: FilesetDirective.java 2119 2005-03-23 02:04:46Z mcconnell@dpml.net $
    */
   public static class Exclude implements Serializable
   {

       /**
        * The base directory
        */
       private final String m_path;

       /**
        * Create an ExcludeDirective instance.
        *
        * @param path the path to include
        */
       public Exclude( final String path )
       {
           m_path = path;
       }

       /**
        * Return the excluded path.
        *
        * @return the path
        */
       public String getPath()
       {
           return m_path;
       }

      /**
       * Test if the supplied object is equal to this object.
       * @param other the object to compare with this instance
       * @return TRUE if the supplied object is equal to this object
       */
       public boolean equals( Object other )
       {
           if( null == other )
           {
               return false;
           }
           else
           {
               if( other instanceof Exclude )
               {
                   Exclude exclude = (Exclude) other;
                   return m_path.equals( exclude.getPath() );
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
           return m_path.hashCode();
       }
   }

   /**
    * <p>An file include directive.</p>
    * <p><b>XML</b></p>
    * <p>An include element is normally contained within a scoping structure such as a
    * fileset or directory set. The include element contains the single attribute name
    * which is used to refer to the file or directory (depending on the containing
    * context.</p>
    * <pre>
    *    <font color="gray">&lt;fileset dir="lib"&gt;</font>
    *       &lt;include name="<font color="darkred">avalon-framework.jar</font>" /&gt;
    *    <font color="gray">&lt;/fileset&gt;</font>
    * </pre>
    *
    * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
    * @version $Id: FilesetDirective.java 2119 2005-03-23 02:04:46Z mcconnell@dpml.net $
    */
    public static class Include implements Serializable
    {

        /**
         * The base directory
         */
        private final String m_path;

        /**
         * Create a IncludeDirective instance.
         *
         * @param path the path to include
         */
        public Include( final String path )
        {
            m_path = path;
        }
    
        /**
         * Return the included path.
         *
         * @return the path
         */
        public String getPath()
        {
            return m_path;
        }

      /**
       * Test if the supplied object is equal to this object.
       * @param other the object to compare with this instance
       * @return TRUE if the supplied object is equal to this object
       */
       public boolean equals( Object other )
       {
           if( null == other )
           {
               return false;
           }
           else
           {
               if( other instanceof Include )
               {
                   Include inc = (Include) other;
                   return m_path.equals( inc.getPath() );
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
           return m_path.hashCode();
       }

    }
}
