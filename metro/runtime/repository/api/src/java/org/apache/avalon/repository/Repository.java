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

package org.apache.avalon.repository;

import java.net.URL;
import javax.naming.directory.Attributes ;

/**
 * A service that provides access to versioned resources.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Repository.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Repository
{
    /**
     * The context resolution key.
     */
    String KEY = "urn:assembly:repository" ;

    /**
     * The seperator character between the group and resource name values
     * in an artifact name.
     */
    String SEPERATOR = ":" ;

   /**
    * Return the metadata of an artifact as attributes.
    * @param artifact the artifact
    * @return the attributes resolved relative to the artifact address
    * @exception RepositoryException if an error occurs while resolving
    *   artifact metadata attributes
    */
    Attributes getAttributes( Artifact artifact ) 
        throws RepositoryException ;
    
    /**
     * Get a resource url relative to the supplied artifact.
     * 
     * @param artifact the artifact describing the resource
     * @return the resource url
     */
    URL getResource( Artifact artifact ) throws RepositoryException;

   /**
    * Return the set of available artifacts capable of providing the  
    * supplied service class.
    *
    * @return the set of candidate factory artifacts
    */
    Artifact[] getCandidates( Class service );

    /**
     * Creates a ClassLoader chain returning the lowest ClassLoader containing 
     * the jar artifact in the loader's path.  The dependencies of the argument 
     * artifact jar and an api, spi and implementation attribute on the jar and 
     * its dependencies are used to construct the ClassLoaders.
     * 
     * @param artifact the implementation artifact
     * @return the lowest ClassLoader in a chain
     * @throws RepositoryException if there is a problem caching and accessing
     * repository artifacts and reading their attributes.
     */
    ClassLoader getClassLoader( Artifact artifact )
        throws RepositoryException ;

    /**
     * Creates a ClassLoader chain returning the lowest ClassLoader containing 
     * the jar artifact in the loader's path.  The dependencies of the argument 
     * artifact jar and an api, spi and implementation attribute on the jar and 
     * its dependencies are used to construct the ClassLoaders.
     * 
     * @param parent the parent classloader
     * @param artifact the implementation artifact
     * @return the lowest ClassLoader in a chain
     * @throws RepositoryException if there is a problem caching and accessing
     * repository artifacts and reading their attributes.
     */
    ClassLoader getClassLoader( ClassLoader parent, Artifact artifact )
        throws RepositoryException ;
}
