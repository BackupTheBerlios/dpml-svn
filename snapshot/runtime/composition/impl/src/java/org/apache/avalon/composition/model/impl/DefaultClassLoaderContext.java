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
import java.net.URL;

import org.apache.avalon.composition.data.ClassLoaderDirective;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.model.ServiceRepository;
import org.apache.avalon.composition.provider.ClassLoaderContext;
import org.apache.avalon.composition.provider.SystemContext;

import org.apache.avalon.repository.Repository;

import org.apache.avalon.extension.manager.OptionalPackage;
import org.apache.avalon.extension.manager.ExtensionManager;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;


/**
 * Implementation of a system context that exposes a system wide set of parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultClassLoaderContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultClassLoaderContext extends DefaultContext 
  implements ClassLoaderContext
{
    //==============================================================
    // static
    //==============================================================

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultClassLoaderContext.class );

    //==============================================================
    // immutable state
    //==============================================================

   /**
    * The logging channel to apply to the classloader model.
    */
    private final Logger m_logger;

   /**
    * The parent classloader.
    */
    private final ClassLoader m_parent;

   /**
    * Option packages established through the chain of parent 
    * models.
    */
    private final OptionalPackage[] m_packages;

   /**
    * The base directory for resolution of extension directories and 
    * fileset directories.
    */
    private final File m_base;

   /**
    * The local jar repository.
    */
    private final Repository m_repository;

   /**
    * The optional extensions package manager.
    */
    private final ExtensionManager m_manager;

   /**
    * The parent type manager.
    */
    private final TypeRepository m_types;

   /**
    * The parent service manager.
    */
    private final ServiceRepository m_services;

   /**
    * The classloader directive.
    */
    private final ClassLoaderDirective m_directive;

   /**
    * Implied url to include in the classpath.
    */
    private final URL[] m_implied;

   /**
    * The system context.
    */
    private final SystemContext m_system;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a root classloader context.
    *
    * @param logger the logging channel to assign to the classloader model
    * @param system the system context
    * @param parent the parent classloader
    * @param directive the classloader directive
    */
    public DefaultClassLoaderContext( 
      Logger logger, SystemContext system, 
      ClassLoader parent, ClassLoaderDirective directive )
    {
        this( logger, system, parent, 
          new OptionalPackage[0], null, null, null, directive, null );
    }

   /**
    * Creation of a new classloader context.
    *
    * @param logger the logging channel to assign to the classloader model
    * @param system the system context
    * @param parent the parent classloader
    * @param packages the set of optional packages established under 
    *    current classloader chain
    * @param manager the optional extions package manager established 
    *    by the parent classloader
    * @param types the parent type manager
    * @param services the parent service manager
    * @param directive the classloader directive
    * @param implied url array added though implication
    */
    public DefaultClassLoaderContext( 
      Logger logger, SystemContext system, 
      ClassLoader parent, OptionalPackage[] packages, 
      ExtensionManager manager, TypeRepository types,
      ServiceRepository services,
      ClassLoaderDirective directive,
      URL[] implied )
    {
        if( logger == null )
        {
            throw new NullPointerException( "logger" );
        }
        if( parent == null )
        {
            throw new NullPointerException( "parent" );
        }
        if( packages == null )
        {
            throw new NullPointerException( "packages" );
        }
        if( directive == null )
        {
            throw new NullPointerException( "directive" );
        }
        if( system == null )
        {
            throw new NullPointerException( "system" );
        }

        m_logger = logger;
        m_repository = system.getRepository();
        m_base = system.getAnchorDirectory();
        m_parent  = parent;
        m_packages = packages;
        m_manager = manager;
        m_types = types;
        m_services = services;
        m_directive = directive;
        m_system = system;

        if( implied == null )
        {
            m_implied = new URL[0];
        }
        else
        {
            m_implied = implied;
        }
    }

    //==============================================================
    // ClassLoaderContext
    //==============================================================

   /**
    * Return the system logging channel.
    *
    * @return the system logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the system context.
    *
    * @return the system context
    */
    public Repository getRepository()
    {
        return m_repository;
    }

   /**
    * Return the base directory from which relative library directives
    * and fileset directory paths may be resolved.
    *
    * @return the base directory
    */
    public File getBaseDirectory()
    {
        return m_base;
    }

   /**
    * Return the classloader to be assigned as the parernt classloader.
    *
    * @return the classloader
    */
    public ClassLoader getClassLoader()
    {
        return m_parent;
    }

   /**
    * Return the containment classloader.
    *
    * @return the classloader
    */
    public OptionalPackage[] getOptionalPackages()
    {
        return m_packages;
    }

   /**
    * Return the extension manager established by the parent 
    * classloader model.
    *
    * @return the extension manager
    */
    public ExtensionManager getExtensionManager()
    {
        return m_manager;
    }

   /**
    * Return the type repository established by the parent classloader.
    *
    * @return the type repository
    */
    public TypeRepository getTypeRepository()
    {
        return m_types;
    }

   /**
    * Return the service repository established by the parent classloader.
    *
    * @return the service repository
    */
    public ServiceRepository getServiceRepository()
    {
        return m_services;
    }

   /**
    * Return the classloader directive to be applied to the 
    * classloader model.
    *
    * @return the classloader directive
    */
    public ClassLoaderDirective getClassLoaderDirective()
    {
        return m_directive;
    }

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
    * Return any implied urls to include in the classloader.
    *
    * @return the implied urls
    */
    public URL[] getImplicitURLs()
    {
        return m_implied;
    } 

}
