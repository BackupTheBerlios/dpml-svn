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

import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DependencyGraph;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.ComponentContext;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;


/**
 * Default implementation of a deployment context that is used
 * as the primary constructor argument when creating a new component
 * model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultComponentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultComponentContext extends DefaultDeploymentContext 
  implements ComponentContext
{
    //==============================================================
    // static
    //==============================================================

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultComponentContext.class );

    //==============================================================
    // immutable state
    //==============================================================

    private final ClassLoader m_classloader;

    private final ComponentProfile m_profile;

    private final Type m_type;

    private final Class m_class;

    private final File m_home;

    private final File m_temp;

    private final ContainmentModel m_model;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a new deployment context.
    *
    * @param logger the logging channel to assign
    * @param name the deployment context name
    * @param system the system context
    * @param classloader the containers classloader
    * @param graph the containers dependency graph
    * @param model the parent containment model
    * @param profile the component deployment profile
    * @param type the underlying component type
    * @param clazz the component deployment class
    * @param home the home working directory
    * @param temp a temporary directory 
    * @param partition the partition name 
    */
    public DefaultComponentContext( 
      Logger logger, String name, SystemContext system, ClassLoader classloader, 
      DependencyGraph graph, ContainmentModel model, ComponentProfile profile, 
      Type type, Class clazz, File home, File temp, 
      String partition )
    {
        super( 
          logger, system, partition, name, profile.getMode(), profile, graph );

        if( partition == null )
        {
            throw new NullPointerException( "partition" );
        }
        if( classloader == null )
        {
            throw new NullPointerException( "classloader" );
        }
        if( clazz == null )
        {
            throw new NullPointerException( "clazz" );
        }
        if( type == null )
        {
            throw new NullPointerException( "type" );
        }
        if( profile == null )
        {
            throw new NullPointerException( "profile" );
        }
        if( model == null )
        {
            throw new NullPointerException( "model" );
        }

        if( home.exists() && !home.isDirectory() )
        {
            final String error = 
              REZ.getString( "deployment.context.home.not-a-directory.error", home  );
            throw new IllegalArgumentException( error );
        }
        if( temp.exists() && !temp.isDirectory() )
        {
            final String error = 
              REZ.getString( "deployment.context.temp.not-a-directory.error", temp  );
            throw new IllegalArgumentException( error );
        }

        m_home = home;
        m_temp = temp;
        m_classloader = classloader;
        m_type = type;
        m_profile = profile;
        m_class = clazz;
        m_model = model;
    }

    //--------------------------------------------------------------
    // ComponentContext
    //--------------------------------------------------------------

   /**
    * Return the enclosing containment model.
    * @return the containment model that component is within
    */
    public ContainmentModel getContainmentModel() 
    {
        return m_model;
    }

   /**
    * Return the working directory.
    *
    * @return the working directory
    */
    public File getHomeDirectory()
    {
        return m_home;
    }

   /**
    * Return the temporary directory.
    *
    * @return the temporary directory
    */
    public File getTempDirectory()
    {
        return m_temp;
    }

   /**
    * Return the deployment profile.
    *
    * @return the profile
    */
    public ComponentProfile getComponentProfile()
    {
        return m_profile;
    }

   /**
    * Return the component type.
    *
    * @return the type defintion
    */
    public Type getType()
    {
        return m_type;
    }

   /**
    * Return the component class.
    *
    * @return the class
    */
    public Class getDeploymentClass()
    {
        return m_class;
    }

   /**
    * Return the classloader for the component.
    *
    * @return the classloader
    */
    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }

}
