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

package org.apache.avalon.composition.model.impl;

import org.apache.avalon.composition.model.DependencyGraph;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.DeploymentContext;
import org.apache.avalon.composition.data.DeploymentProfile;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.composition.data.Mode;

/**
 * Default implementation of a deployment context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultDeploymentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultDeploymentContext extends DefaultContext 
  implements DeploymentContext
{
    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    private final String m_name;
    private final String m_partition;
    private final Logger m_logger;
    private final Mode m_mode;
    private final DependencyGraph m_graph;
    private final SystemContext m_system;
    private final DeploymentProfile m_profile;

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

   /**
    * Creation of a new deployment context.
    *
    * @param logger the logging channel to assign
    * @param partition the assigned partition name
    * @param name the profile name
    * @param mode the deployment mode
    * @param graph the parent deployment assembly graph
    */
    public DefaultDeploymentContext( 
      Logger logger, SystemContext system, String partition, String name, 
      Mode mode, DeploymentProfile profile, DependencyGraph graph )
    {
        if( logger == null )
        {
            throw new NullPointerException( "logger" );
        }
        if( name == null )
        {
            throw new NullPointerException( "name" );
        }
        if( profile == null )
        {
            throw new NullPointerException( "mode" );
        }
        if( profile == null )
        {
            throw new NullPointerException( "profile" );
        }
        if( system == null )
        {
            throw new NullPointerException( "system" );
        }

        m_profile = profile;
        m_graph = new DependencyGraph( graph );
        if( graph != null )
        {
            graph.addChild( m_graph );
        }

        m_logger = logger;
        m_system = system;
        m_partition = partition;
        m_name = name;
        m_mode = mode;
    }

    //---------------------------------------------------------
    // DeploymentContext
    //---------------------------------------------------------

   /**
    * Return the system context.
    *
    * @return the system context
    */
    public SystemContext getSystemContext()
    {
        return m_system;
    }

   /**
    * Return the profile name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the assigned partition name.
    *
    * @return the partition name
    */
    public String getPartitionName()
    {
        return m_partition;
    }

   /**
    * Return the model fully qualified name.
    * @return the fully qualified name
    */
    public String getQualifiedName()
    {
        if( null == getPartitionName() )
        {
            return SEPARATOR;
        }
        else
        {
            return getPartitionName() + getName();
        }
    }

   /**
    * Return the mode of establishment.
    * @return the mode
    */
    public Mode getMode()
    {
        return m_mode;
    }

   /**
    * Return the deployment profile.
    *
    * @return the profile
    */
    public DeploymentProfile getProfile()
    {
        return m_profile;
    }

   /**
    * Return the assigned logger.
    * @return the logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the dependency graph used to construct 
    * deployment and decommissioning sequences.
    *
    * @return the dependency graph
    */
    public DependencyGraph getDependencyGraph()
    {
        return m_graph;
    }
}
