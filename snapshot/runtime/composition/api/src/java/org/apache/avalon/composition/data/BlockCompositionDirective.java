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

import org.apache.avalon.repository.Artifact;

/**
 * A block reference directive contains an identifier and verion of 
 * a local resource to be included by reference into 
 * a container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: BlockCompositionDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class BlockCompositionDirective extends DeploymentProfile
{
    /**
     * The version identifier.
     */
    private final Artifact m_artifact;

    /**
     * Nested targets.
     */
    private final TargetDirective[] m_targets;

    /**
     * Creation of a new resource directive.
     * @param name the name to assign to the container 
     *   established by the composition directive
     * @param artifact an artifact from which a block 
     *   description can be resolved
     */
    public BlockCompositionDirective( 
      final String name, Artifact artifact )
    {
        this( name, artifact, new TargetDirective[0] );
    }

    /**
     * Creation of a new resource directive.
     * @param name the name to assign to the container 
     *   established by the composition directive
     * @param artifact an artifact from which a block 
     *   description can be resolved
     */
    public BlockCompositionDirective( 
      final String name, Artifact artifact, TargetDirective[] targets )
    {
        super( name, DeploymentProfile.ENABLED, Mode.EXPLICIT, null );
        if( artifact == null )
        {
            throw new NullPointerException( "artifact" );
        }
        m_artifact = artifact;
        m_targets = targets;
    }

    /**
     * Return the artifact reference.
     * @return the artifact
     */
    public Artifact getArtifact()
    {
        return m_artifact;
    }

    /**
     * Return the relative targets.
     * @return the targets
     */
    public TargetDirective[] getTargetDirectives()
    {
        return m_targets;
    }
}
