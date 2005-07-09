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

package net.dpml.test.layout;

import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.runtime.Layout;

/** 
 * A layout manager used to vaidate plugin layout handlers in Transit.
 * The implementation is a strait copy of the classic layout handler.
 */
public class DemoLayout implements Layout
{
    /**
     * Return the base path for an artifact.  The base path is derived from
     * the artifact group and type.  For an artifact group of "metro/cache" and a
     * type equal to "jar", the base value will be translated using the pattern
     * "[group]/[type]s" to form "metro/cache/jars".  The base path value represents
     * the directory path relative to a repository root of the directory containing
     * this artifact.
     *
     * @return the base path
     */
    public final String resolveBase( Artifact artifact )
    {
        return artifact.getGroup() + "/" + artifact.getType() + "s";
    }

    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The full path is equivalent to the base path and artifact filename using the
     * pattern "[base]/[filename]".  Path values may be used to resolve an artifact
     * from a remote repository or local cache relative to the repository or cache
     * root. An artifact such as <code>artifact:jar:metro/cache/dpml-cache-main#1.0.0</code>
     * would return the path <code>metro/cache/jars/dpml-cache-main-1.0.0.jar</code>.
     *
     * @see #resolveBase
     * @see #resolveFilename
     * @return the logical artifact path
     */
    public final String resolvePath( Artifact artifact )
    {
        return resolveBase( artifact ) + "/" + resolveFilename( artifact );
    }

    /**
     * Return the expanded filename of the artifact. The filename is expressed
     * as [name]-[version].[type] or in case of a null version simply [name].[type].
     *
     * @return the artifact expanded filename
     */
    public String resolveFilename( Artifact artifact )
    {
        String ver = artifact.getVersion();
        if( null == ver )
        {
            return artifact.getName() + "." + artifact.getType();
        }
        else
        {
            return artifact.getName() + "-" + ver + "." + artifact.getType();
        }
    }

}
