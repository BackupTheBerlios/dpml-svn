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
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.DependencyGraph;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.ContainmentContext;
import org.apache.avalon.composition.provider.ComponentContext;

import org.apache.avalon.logging.provider.LoggingManager;
import org.apache.avalon.logging.data.CategoriesDirective;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A utility class that handles creation of a component model context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultContainmentModelComponentHelper.java 30977 2004-07-30 08:57:54Z niclas $
 */
class DefaultContainmentModelComponentHelper
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultContainmentModelComponentHelper.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ContainmentContext m_context;
    private final ContainmentModel m_model;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Creation of a component context creation helper.
    * @param context the containment model context
    * @param model the containment model
    */
    public DefaultContainmentModelComponentHelper( 
       ContainmentContext context, ContainmentModel model )
    {
        if( context == null )
        {
            throw new NullPointerException( "context" );
        }
        if( model == null )
        {
            throw new NullPointerException( "model" );
        }
        m_context = context;
        m_model = model;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Creation of a new component model relative to a supplied profile.
    *
    * @param profile the component profile
    * @return the component model context
    */
    public ComponentContext createComponentContext( final ComponentProfile profile )
      throws ModelException
    {
        if( null == profile )
        {
            throw new NullPointerException( "profile" );
        }

        SystemContext system = m_context.getSystemContext();
        final String name = profile.getName();
        final String partition = m_model.getPartition();
        LoggingManager logging = system.getLoggingManager();
        CategoriesDirective categories = profile.getCategories();
        if( null != categories )
        {
            logging.addCategories( partition, categories );
        }

        Logger logger = 
          logging.getLoggerForCategory( partition + name );
        DependencyGraph graph = m_context.getDependencyGraph();
        ClassLoader classloader = m_context.getClassLoader();
        final File home = new File( m_context.getHomeDirectory(), name );
        final File temp = new File( m_context.getTempDirectory(), name );

        try
        {
            Class base = classloader.loadClass( profile.getClassname() );
            Type type = 
              m_model.getClassLoaderModel().getTypeRepository().getType( base );

            return new DefaultComponentContext( 
                logger, 
                name, 
                system, 
                classloader, 
                graph, 
                m_model, 
                profile, 
                type, 
                base, 
                home, 
                temp, 
                partition );

        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "containment.deployment.create.error", 
                m_model.getPath(), 
                name );
            throw new ModelException( error, e );
        }
    }
}
