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

package org.apache.avalon.composition.provider;

import java.io.File;
import java.util.Map;

import org.apache.avalon.composition.data.SecurityProfile;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;

import org.apache.avalon.framework.context.Context;


/**
 * Defintion of a system context that exposes a system wide set of parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SystemContextFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface SystemContextFactory 
{
   /**
    * Set the security enabled status.
    * @param secure the security enabled flag
    */
    void setSecurityEnabled( boolean secure );

   /**
    * Set the parent context.
    * @param context a parent context instance 
    */
    void setParentContext( Context context );

   /**
    * Set the runtime using a supplied artifact.
    * @param artifact a factory artifact supporting 
    *    {@link Runtime} instance creation. 
    * @see Runtime
    * @see #setRuntime( Class )
    */
    void setRuntime( Artifact artifact );

   /**
    * Set the lifestyle factory using a supplied artifact.
    * @param artifact a factory artifact supporting 
    *    {@link LifestyleFactory} instance creation. 
    * @see Runtime
    * @see #setRuntime( Class )
    */
    void setLifestyleArtifact( Artifact artifact );

   /**
    * Set the runtime using a supplied class.
    * @param clazz a runtime implementation class 
    * @see Runtime
    */
    void setRuntime( Class clazz );

   /**
    * Set the application repository.
    * @param repository the application repository
    */
    void setRepository( Repository repository );

   /**
    * Set the logging manager.
    * @param logging the logging manager
    */
    void setLoggingManager( LoggingManager logging );

   /**
    * Set the system wide default deployment timeout.
    * @param timeout the timeout value in milliseconds
    */
    void setDefaultDeploymentTimeout( long timeout );

   /**
    * Set the system wide default for proxy creation.  The default value
    * is TRUE enabling automation of component finalization and improved
    * memory management.
    *
    * @param flag if TRUE (the default) proxy generation is enabled on
    *   all components otherwise proxy creation is disabled.
    */
    void setDefaultProxyPolicy( boolean flag );

   /**
    * Set the system trace flag.
    * @param trace the trace flag
    */
    void setTraceEnabled( boolean trace );

   /**
    * Set the security profiles.
    * @param profiles the security profiles
    */
    void setSecurityProfiles( SecurityProfile[] profiles );

   /**
    * Set the initial grants table.
    * @param grants the initial grants table
    */
    void setGrantsTable( Map grants );

   /**
    * Set the working directory.
    * @param work the working directory
    */
    void setWorkingDirectory( File work );

   /**
    * Set the temporary directory.
    * @param temp the temporary directory
    */
    void setTemporaryDirectory( File temp );

   /**
    * Set the anchor directory.
    * @param anchor the anchor directory
    */
    void setAnchorDirectory( File anchor );

   /**
    * Set the name of the logging channel to be used by the 
    * system context.
    * @param name the name to assign
    */
    void setName( String name );

   /**
    * Creation of a new system context using supplied and default
    * values.
    * @return a new system context instance
    * @exception SystemException if a stytem context creation error occurs
    */
    SystemContext createSystemContext()
      throws SystemException;
}
