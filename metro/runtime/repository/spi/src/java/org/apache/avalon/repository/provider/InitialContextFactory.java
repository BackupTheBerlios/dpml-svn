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

import org.apache.avalon.repository.Artifact;


/**
 * Utility interface that provides support for the creation of a 
 * new initial context.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: InitialContextFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface InitialContextFactory
{
   /**
    * An array of property keys that are used to locate default
    * values.
    * 
    * @see InitialContext#IMPLEMENTATION_KEY
    * @see InitialContext#CACHE_KEY
    * @see InitialContext#HOSTS_KEY
    * @see InitialContext#ONLINE_KEY
    * @see InitialContext#FACTORY_ARTIFACTS_KEY
    */
    String[] KEYS = 
      new String[]{
        InitialContext.ONLINE_KEY,
        InitialContext.IMPLEMENTATION_KEY,
        InitialContext.CACHE_KEY,
        InitialContext.HOSTS_KEY,
        InitialContext.FACTORY_ARTIFACTS_KEY };

   /**
    * Return the home directory value direved from the application key.
    * @return the home directory.
    */
    File getHomeDirectory();

   /**
    * Register a set of factory artifacts.
    * @param artifacts the factory artifact references
    */
    void setFactoryArtifacts( Artifact[] artifacts );

   /**
    * Set the online mode of the repository. The default policy is to 
    * to enable online access to remote repositories.  Setting the onLine
    * mode to false disables remote repository access.   
    *
    * @param policy the online connected policy
    */
    void setOnlineMode( boolean policy );

   /**
    * Set the parent classloader.
    *
    * @param classloader the parent classloader
    */
    void setParentClassLoader( ClassLoader classloader );

   /**
    * The initial context factory support the establishment of an 
    * initial context which is associated with a repository cache 
    * manager implementation.  A client can override the default
    * repository cache manager implementation by declaring an 
    * artifact referencing a compliant factory (not normally 
    * required).
    *
    * @param artifact the repository cache manager artifact
    */
    void setImplementation( Artifact artifact );

   /**
    * The cache directory is the directory into which resources 
    * such as jar files are loaded by a repository cache manager.
    *
    * @param cache the repository cache directory
    */
    void setCacheDirectory( File cache );

   /**
    * Set the proxy host name.  If not supplied proxy usage will be 
    * disabled.
    *
    * @param host the proxy host name
    */
    void setProxyHost( String host );

   /**
    * Set the proxy host port.
    *
    * @param port the proxy port
    */
    void setProxyPort( int port );

   /**
    * Set the proxy username.
    *
    * @param username the proxy username
    */
    void setProxyUsername( String username );

   /**
    * Set the proxy account password.
    *
    * @param password the proxy password
    */
    void setProxyPassword( String password );

   /**
    * Set the initial hosts to be used by a repository cache manager 
    * implementation and the initial context implementation when 
    * resolving dependent resources.  If is resource is not present
    * in a local cache, remote hosts are checked in the order presented
    * in the supplied list. A host may be a file url or a http url.
    *
    * @param hosts a sequence of remote host urls
    */
    void setHosts( String[] hosts );

   /**
    * Creation of an inital context based on the system and working 
    * directory, parent classloader, repository cache manager implementation
    * artifact, cache directory, and remote hosts sequence supplied to the 
    * factory.
    *
    * @return a new initial context
    */
    InitialContext createInitialContext();
}
