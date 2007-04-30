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

package net.dpml.transit.layout;

import net.dpml.transit.Artifact;
import net.dpml.transit.Layout;
import net.dpml.transit.TransitRuntimeException;

/** 
 * The ClassicLayout decodes artifacts into the Classic/Maven layout
 * of artifacts on a file system or http server.
 * This format says that for an artifact <code>artifact:[type]:[group]/[name]#[version]</code>
 * the location of such artifact would be;
 * <code>[group]/[type]s/[name]-[version].[type]</code>.
 * Example; <code>artifact:jar:metro/cache/dpml-cache-main#1.0.0</code>
 * would return the path <code>metro/cache/jars/dpml-cache-main-1.0.0.jar</code>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ClassicLayout extends Layout
{
    private static final String CLASSIC_IDENTIFIER = "classic";
    
    /**
     * Return the layout identifier.  The id value is used
     * to identify layout instances assigned to cache handlers and 
     * resource host handlers.
     *
     * @return the layout id
     */
    public String getID()
    {
        return CLASSIC_IDENTIFIER;
    }
    
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
        if( null == artifact.getGroup() )
        {
            return artifact.getType() + "s";
        }
        else
        {
            return artifact.getGroup() + "/" + artifact.getType() + "s";
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
