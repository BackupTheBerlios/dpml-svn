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

package org.apache.avalon.repository.provider;

import java.io.File;
import java.util.Map;

import org.apache.avalon.repository.Artifact;

/**
 * Interface defining the operations available to manipulate repository
 * factory criteria.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: RepositoryCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface RepositoryCriteria extends Map
{
   /**
    * Online mode.
    */
    String REPOSITORY_ONLINE_MODE = InitialContext.ONLINE_KEY;

   /**
    * Repository cache directory parameter descriptor.
    */
    String REPOSITORY_CACHE_DIR = InitialContext.CACHE_KEY;

   /**
    * Repository proxy password parameter descriptor.
    */
    String REPOSITORY_REMOTE_HOSTS = InitialContext.HOSTS_KEY;

   /**
    * Repository proxy password parameter descriptor.
    */
    String REPOSITORY_FACTORY_ARTIFACTS = 
      InitialContext.FACTORY_ARTIFACTS_KEY;

   /**
    * An array of property keys that are used to locate default
    * values.
    */
    String[] KEYS = 
      new String[]{
        REPOSITORY_ONLINE_MODE,
        REPOSITORY_CACHE_DIR,
        REPOSITORY_REMOTE_HOSTS,
        REPOSITORY_FACTORY_ARTIFACTS };

   /**
    * Set the online mode of the repository. The default policy is to 
    * to enable online access to remote repositories.  Setting the on-line
    * mode to false disables remote repository access.   
    *
    * @param policy the online connected policy
    */
    void setOnlineMode( boolean policy );

   /**
    * The cache directory is the directory into which resources 
    * such as jar files are loaded by a repository.
    *
    * @param cache the repository cache directory
    */
    void setCacheDirectory( File cache );

   /**
    * Set the hosts to be used by a repository cache manager 
    * implementation and the initial context implementation when 
    * resolving dependent resources.  If is resource is not present
    * in a local cache, remote hosts are checked in the order presented
    * in the supplied list. A host may be a file url or a http url.
    *
    * @param hosts a sequence of remote host urls
    */
    void setHosts( String[] hosts );

   /**
    * Set the available factory artifacts.  Each artifact represents a 
    * resolvable factory artifiact (artifact with an associate meta 
    * descriptor) than can be used as the basis for classloader creation
    * and instance establishment.
    *
    * @param artifacts a sequence of artifact identifiers
    */
    void setFactoryArtifacts( Artifact[] artifacts );
}
