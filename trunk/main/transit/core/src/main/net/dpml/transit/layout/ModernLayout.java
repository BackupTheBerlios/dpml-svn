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
 * The ModernLayout decodes artifacts into a layout scheme that follows the
 * convention of group/name/version/expanded-name pattern.  Specifically the 
 * layout maps artifacts according to the rule
 * [group[/[subgroup[/...]]]/[name]/[version]/name[-[version]].type.
 * Example: <code>artifact:jar:metro/cache/dpml-cache-main#1.0.0</code>
 * would return the path <code>metro/cache/dpml-cache-main/1.0.0/dpml-cache-main-1.0.0.jar</code>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModernLayout
    extends Layout
{
    private static final String LAYOUT_IDENTIFIER = "modern";
    
    /**
     * Return the layout identifier.  The id value is used
     * to identify layout instances assigned to cache handlers and 
     * resource host handlers.
     *
     * @return the layout id
     */
    public String getID()
    {
        return LAYOUT_IDENTIFIER;
    }
    
    /**
     * Return the base path for an artifact.  The base path is derived from
     * the artifact group and type.  The base path value represents
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
                return artifact.getName();
            }
            else
            {
                String group = getGroupPath( artifact );
                return group + "/" + artifact.getName();
            }
        }
        else
        {
            if( null == artifact.getGroup() )
            {
                return artifact.getName() + "/" + version;
            }
            else
            {
                String group = getGroupPath( artifact );
                return group + "/" + artifact.getName() + "/" + version;
            }
        }
    }
    
    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The full path is equivalent to the base path and artifact filename using the
     * pattern "[base]/[filename]".  Path values may be used to resolve an artifact
     * from a remote repository or local cache relative to the repository or cache
     * root.
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
    
   /**
    * To be compatible with the maven-2 strategy we need to subsitute period 
    * characters with a group separator.
    * @param artifact the artifact from which to resolve the group path
    * @return the group path
    */
    private String getGroupPath( Artifact artifact )
    {
        String group = artifact.getGroup();
        return group.replace( '.', '/' );
    }
}
