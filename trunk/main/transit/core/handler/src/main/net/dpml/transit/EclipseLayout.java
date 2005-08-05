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
 * The EclipseLayout decodes artifacts into the Eclipse specified layout
 * of artifacts on a file system or http server.
 * This format says that for an artifact <code>artifact:[type]:[group]/[name]#[version]</code>
 * the location of such artifact would be;
 * <code>[group]-[version]/[name].[type]</code>.
 * Example; <code>artifact:jar:eclipse/plugins/eclipse-osgi-runtime/core#3.1.0</code>
 * would return the path <code>eclipse/plugins/eclipse-osgi-runtime-3.1.0/core.jar</code>.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class EclipseLayout extends AbstractLayout
    implements Layout
{
    /**
     * Return the base path for an artifact.  The base path is derived from
     * the artifact group and version.  For an artifact group of "metro/cache" and a
     * version equal to "1.3", the base value will be translated using the pattern
     * "[group]-[version]" to form "metro/cache-1.3".  The base path value represents
     * the directory path relative to a repository root of the directory containing
     * this artifact.
     *
     * @param artifact the artifact to resolve the base path from
     * @return the base path
     */
    public final String resolveBase( Artifact artifact )
    {
        return artifact.getGroup() + "-" + artifact.getVersion();
    }

    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The full path is equivalent to the base path and artifact filename using the
     * pattern "[base]/[filename]".  Path values may be used to resolve an artifact
     * from a remote repository or local cache relative to the repository or cache
     * root. An artifact such as
     * <code>artifact:jar:eclipse/plugins/eclipse-osgi-runtime/core#3.1.0</code>
     * would return the path
     * <code>eclipse/plugins/eclipse-osgi-runtime-3.1.0/core.jar</code>.
     *
     * @param artifact the artifact to resolve the path from
     * @see #resolveBase
     * @see #resolveFilename
     * @return the logical artifact path
     */
    public final String resolvePath( Artifact artifact )
    {
        return resolveBase( artifact ) + "/" + resolveFilename( artifact );
    }

    /**
     * Return the expanded filename of the artifact.
     * The filename is expressed as <code>[name].[type]</code>.
     *
     * @param artifact the artifact to resolve
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

    /**
     * Return the expanded filename of the artifact.
     * The filename is expressed as <code>[name].[type]</code>.
     *
     * @param artifact the artifact to resolve
     * @return the artifact expanded filename
     */
    public String resolveBaseFilename( Artifact artifact )
    {
        return artifact.getName() + "." + artifact.getType();
    }
}
