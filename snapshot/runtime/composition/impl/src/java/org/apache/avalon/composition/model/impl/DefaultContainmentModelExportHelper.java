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

import org.apache.avalon.composition.data.ServiceDirective;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.ModelRepository;
import org.apache.avalon.composition.model.ModelSelector;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.provider.ContainmentContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.meta.info.DependencyDescriptor;

/**
 * A utility class that assists in the location of a model relative
 * a supplied path.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultContainmentModelExportHelper.java 30977 2004-07-30 08:57:54Z niclas $
 */
class DefaultContainmentModelExportHelper
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultContainmentModelExportHelper.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ContainmentContext m_context;
    private final ContainmentModel m_model;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public DefaultContainmentModelExportHelper( 
      ContainmentContext context, ContainmentModel model )
    {
        m_context = context;
        m_model = model;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    public DefaultServiceModel[] createServiceExport() throws ModelException
    {

        ServiceDirective[] export = 
          m_context.getContainmentProfile().getExportDirectives();
        DefaultServiceModel[] services = new DefaultServiceModel[ export.length ];
        for( int i=0; i<export.length; i++ )
        {
            ServiceDirective service = export[i];
            Class clazz = getServiceExportClass( service );
            DeploymentModel provider = 
              locateImplementionProvider( service );
            services[i] = 
              new DefaultServiceModel( service, clazz, provider ); 
        }
        return services;
    }

   /**
    * Return the class declared by a container service export declaration.
    * @return the exported service interface class
    * @exception ModelException if the class cannot be resolved
    */
    private Class getServiceExportClass( ServiceDirective service )
      throws ModelException
    {
        String classname = service.getReference().getClassname();
        try
        {
            ClassLoader classloader = m_context.getClassLoader();
            return classloader.loadClass( classname );
        }
        catch( Throwable e )
        {
            final String error = 
              "Cannot load service class [" 
              + classname 
              + "].";
            throw new ModelException( error, e );
        }
    }

   /**
    * Given a service directive declared by a container, locate a model 
    * with this containment model to map as the provider.  If not model
    * is explicity declared, the implementation will attempt to construct
    * a new model based on packaged profiles and add the created model to
    * the set of models within this container.
    * 
    * @param service the service directive
    * @return the implementing deployment model
    * @exception ModelException if an implementation is not resolvable 
    */
    private DeploymentModel locateImplementionProvider( ServiceDirective service )
      throws ModelException
    {
        final String path = service.getPath();
        if( null != path )
        {
            DeploymentModel provider = m_model.getModel( path );
            if( null == provider )
            {
                final String error = 
                  "Implemention provider path [" 
                  + path 
                  + "] for the exported service [" 
                  + service.getReference()
                  + "] in the containment model "
                  + m_model
                  + " does not reference a known model.";
               throw new ModelException( error );
            }
            else
            {
                return provider;
            }
        }
        else
        {
            final DependencyDescriptor dependency = 
              new DependencyDescriptor( 
                "export", 
                service.getReference() );

            final ModelRepository repository = m_context.getModelRepository();
            final DeploymentModel[] candidates = repository.getModels();
            final ModelSelector selector = new DefaultModelSelector();
            DeploymentModel provider = selector.select( candidates, dependency );
            if( null != provider )
            {
                return provider;
            }
            else
            {
                TypeRepository repo = 
                  m_context.getClassLoaderModel().getTypeRepository();
                DeploymentProfile profile = 
                  repo.getProfile( dependency, false );
                if( profile != null )
                {
                    return m_model.addModel( profile );
                }
                else
                {
                    final String error = 
                      "Could not locate a provider for the exported service [" 
                        + dependency.getReference()
                        + "] in the containment model "
                        + m_model;
                    throw new ModelException( error );
                }
            }
        }
    }

}
