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

/**
 * A block include directive that references a source file describing a block.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: BlockIncludeDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class BlockIncludeDirective extends DeploymentProfile
{
    /**
     * The include path.
     */
    private final String m_path;

    /**
     * Creation of a new entry directive.
     * @param name the name to assign to the included container
     * @param path a relative path to the block descriptor
     */
    public BlockIncludeDirective( final String name, final String path )
    {
        super( name, DeploymentProfile.ENABLED, Mode.EXPLICIT, null );
        if( path == null )
        {
            throw new NullPointerException( "path" );
        }
        m_path = path;
    }

    /**
     * Return the containment include path.
     *
     * @return the path
     */
    public String getPath()
    {
        return m_path;
    }
}
