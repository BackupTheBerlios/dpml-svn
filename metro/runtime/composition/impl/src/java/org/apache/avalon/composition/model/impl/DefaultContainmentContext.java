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

import java.io.File;

import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.model.DependencyGraph;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ClassLoaderModel;
import org.apache.avalon.composition.model.ModelRepository;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.ContainmentContext;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;


/**
 * Implementation of a containment supplied to a containment model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultContainmentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultContainmentContext extends DefaultDeploymentContext 
  implements ContainmentContext
{
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultContainmentContext.class );

    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    private final File m_home;

    private final File m_temp;

    private final ClassLoaderModel m_classloader;

    private final ContainmentProfile m_profile;

    private final ModelRepository m_repository;

    private final ContainmentModel m_parent;

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

   /**
    * Creation of a new root containment context.
    *
    * @param logger the logging channel to assign
    * @param system the system context
    * @param model the classloader model
    * @param repository the parent model repository
    * @param graph the parent dependency graph
    * @param profile the containment profile
    */
    public DefaultContainmentContext( 
      Logger logger, SystemContext system, ClassLoaderModel model, 
      ModelRepository repository, DependencyGraph graph, 
      ContainmentProfile profile )
    {
        this( logger, system, model, repository, graph,
          system.getHomeDirectory(), system.getTempDirectory(), 
          null, profile, null, "" );
    }

   /**
    * Creation of a new containment context.
    *
    * @param logger the logging channel to assign
    * @param system the system context
    * @param model the classloader model
    * @param repository the parent model repository
    * @param graph the parent dependency graph
    * @param home the directory for the container
    * @param temp a temporary directory for the container
    * @param profile the containment profile
    * @param partition the partition that this containment
    *    context is established within
    * @param name the assigned containment context name
    */
    public DefaultContainmentContext( 
      Logger logger, SystemContext system, ClassLoaderModel model, 
      ModelRepository repository, DependencyGraph graph, 
      File home, File temp, ContainmentModel parent, 
      ContainmentProfile profile, String partition, String name )
    {
        super( logger, system, partition, name, profile.getMode(), profile, graph );

        if( system == null )
        {
            throw new NullPointerException( "system" );
        }
        if( model == null )
        {
            throw new NullPointerException( "model" );
        }
        if( home == null )
        {
            throw new NullPointerException( "home" );
        }
        if( temp == null )
        {
            throw new NullPointerException( "temp" );
        }
        if( profile == null )
        {
            throw new NullPointerException( "profile" );
        }

        if( home.exists() && !home.isDirectory() )
        {
            final String error = 
              REZ.getString( 
                "containment.context.home.not-a-directory.error", home.toString() );
            throw new IllegalArgumentException( error );
        }
        if( temp.exists() && !temp.isDirectory() )
        {
            final String error = 
              REZ.getString( 
                "containment.context.temp.not-a-directory.error", temp.toString() );
            throw new IllegalArgumentException( error );
        }

        m_repository = new DefaultModelRepository( repository, logger );

        m_classloader = model;
        m_home = home;
        m_temp = temp;
        m_parent = parent;
        m_profile = profile;
    }

    //---------------------------------------------------------
    // ContainmentContext
    //---------------------------------------------------------

   /**
    * Return the working directory from which containers may 
    * establish persistent content.
    *
    * @return the working directory
    */
    public File getHomeDirectory()
    {
        return m_home;
    }

   /**
    * Return the temporary directory from which a container 
    * may use to establish a transient content directory. 
    *
    * @return the temporary directory
    */
    public File getTempDirectory()
    {
        return m_temp;
    }

   /**
    * Return the containment profile.
    *
    * @return the containment profile
    */
    public ContainmentProfile getContainmentProfile()
    {
        return m_profile;
    }

   /**
    * Return the model repository.
    *
    * @return the model repository
    */
    public ModelRepository getModelRepository()
    {
        return m_repository;
    }

   /**
    * Return the containment classloader model.
    *
    * @return the classloader model
    */
    public ClassLoaderModel getClassLoaderModel()
    {
        return m_classloader;
    }

   /**
    * Return the containment classloader.  This method is a 
    * convinience operation equivalent to 
    * getClassLoaderModel().getClassLoader(); 
    *
    * @return the classloader
    */
    public ClassLoader getClassLoader()
    {
        return m_classloader.getClassLoader();
    }

   /**
    * Return the parent containment model.
    *
    * @return the model parent container
    */
    public ContainmentModel getParentContainmentModel()
    {
        return m_parent;
    }


}
