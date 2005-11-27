/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

/** 
 * The ModernLayout decodes artifacts into a layout scheme that follows the 
 * [group[/[subgroup[/...]]]/[version]/name[-[version]].type convention.
 * Example; <code>artifact:jar:metro/cache/dpml-cache-main#1.0.0</code>
 * would return the path <code>metro/cache/1.0.0/dpml-cache-main-1.0.0.jar</code>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModernLayout
    implements Layout
{
    /**
     * Return the base path for an artifact.  The base path is derived from
     * the artifact group and type.  For an artifact group of "metro/cache" and a
     * type equal to "jar", the base value will be translated using the pattern
     * "[group]/[type]s" to form "metro/cache/jars".  The base path value represents
     * the directory path relative to a repository root of the directory containing
     * this artifact.
     *
     * @param artifact the resource artifact
     * @return the base path
     */
    public final String resolveBase( Artifact artifact )
    {
        String version = artifact.getVersion();
        if( null == version )
        {
            if( null == artifact.getGroup() )
            {
                return "";
            }
            else
            {
                return artifact.getGroup();
            }
        }
        else
        {
            if( null == artifact.getGroup() )
            {
                return version;
            }
            else
            {
                return artifact.getGroup() + "/" + version;
            }
        }
    }
    
    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The full path is equivalent to the base path and artifact filename using the
     * pattern "[base]/[filename]".  Path values may be used to resolve an artifact
     * from a remote repository or local cache relative to the repository or cache
     * root. An artifact such as <code>artifact:jar:metro/cache/dpml-cache-main#1.0.0</code>
     * would return the path <code>metro/cache/jars/dpml-cache-main-1.0.0.jar</code>.
     *
     * @param artifact the resource artifact
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
     * @param artifact the resource artifact
     * @return the artifact expanded filename
     */
    public String resolveFilename( Artifact artifact )
    {
        String scheme = artifact.getScheme();
        String filename = resolveBaseFilename( artifact );
        if( "artifact".equals( scheme ) )
        {
            return filename;
        }
        else if( "link".equals( scheme ) )
        {
            return filename + ".link";
        }
        else
        {
            final String error = 
              "Protocol not recognized: " + scheme;
            throw new TransitRuntimeException( error );
        }
    }

    private String resolveBaseFilename( Artifact artifact )
    {
        String version = artifact.getVersion();
        if( null == version )
        {
            return artifact.getName() + "." + artifact.getType();
        }
        else
        {
            return artifact.getName() + "-" + version + "." + artifact.getType();
        }
    }
}
