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

import org.apache.avalon.repository.Artifact;

/**
 * Description of classpath.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ClasspathDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ClasspathDirective implements Serializable
{
     private static final FilesetDirective[] EMPTY_FILESETS = 
       new FilesetDirective[0]; 
     private static final Artifact[] EMPTY_ARTIFACTS = 
       new Artifact[0]; 

    /**
     * The fileset directives
     */
    private FilesetDirective[] m_filesets;

    /**
     * The resource references
     */
    private Artifact[] m_artifacts;

    /**
     * Create a empty ClasspathDirective.
     */
    public ClasspathDirective()
    {
        this( null, null );
    }

    /**
     * Create a ClasspathDirective instance.
     *
     * @param filesets the filesets to be included in a classloader
     * @param artifacts the set of artifact directives
     */
    public ClasspathDirective( 
       final FilesetDirective[] filesets, 
       final Artifact[] artifacts )
    {
        if( filesets == null )
        {
            m_filesets = EMPTY_FILESETS;
        }
        else
        {
            m_filesets = filesets;
        }

        if( artifacts == null )
        {
            m_artifacts = EMPTY_ARTIFACTS;
        }
        else
        {
            m_artifacts = artifacts;
        }
    }

   /**
    * Return the default status of this directive.  If TRUE
    * the enclosed repository and fileset directives are empty.
    */
    public boolean isEmpty()
    {
        final int n = m_artifacts.length + m_filesets.length;
        return n == 0;
    }

    /**
     * Return the set of artifact directives.
     *
     * @return the artifact directive set
     */
    public Artifact[] getArtifacts()
    {
        return m_artifacts;
    }

    /**
     * Return the set of fileset directives.
     *
     * @return the fileset directives
     */
    public FilesetDirective[] getFilesets()
    {
        return m_filesets;
    }
}
