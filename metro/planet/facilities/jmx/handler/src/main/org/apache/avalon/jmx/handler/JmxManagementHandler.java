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
package org.apache.avalon.jmx.handler;

import org.apache.avalon.composition.event.CompositionEvent;
import org.apache.avalon.composition.event.CompositionListener;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.jmx.ComponentRegistrationManager;
import org.apache.avalon.jmx.ComponentRegistrationException;

/**
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $
 *
 * @avalon.component name="jmx-handler" lifestyle="singleton"
 */
public class JmxManagementHandler 
    implements CompositionListener, Disposable
{
    private static final Resources REZ = ResourceManager.getPackageResources(
        JmxManagementHandler.class );
        
    private ContainmentModel m_model;
    private ComponentRegistrationManager m_manager;
    private Logger m_logger;

    /**
     * Creation of a new handler.
     *
     * @param logger the logging channel
     * @param context the Context for this component
     * @avalon.entry key="urn:composition:containment.model" 
     *   type="org.apache.avalon.composition.model.ContainmentModel"
     * @param manager the ServiceManager from which to retrieve service dependencies
     * @avalon.dependency key="registry" 
     *    type="org.apache.avalon.jmx.ComponentRegistrationManager" 
     * @throws ContextException if a required context entry is not available
     * @throws ServiceException if a required service is not available
     */
    public JmxManagementHandler( 
      Logger logger, Context context, ServiceManager manager ) 
      throws ContextException, ServiceException
    {
        m_logger = logger;
        m_model = 
          (ContainmentModel) context.get( 
            "urn:composition:containment.model" );
        m_manager = 
          (ComponentRegistrationManager) manager.lookup( "registry" );
        synchronized ( m_model )
        {
            processModel( m_model, true );
        }
    }

    public void dispose()
    {
        synchronized ( m_model )
        {
            processModel( m_model, false );
        }
    }

    /**
     * modelAdded
     *
     * @param compositionEvent CompositionEvent
     */
    public void modelAdded( CompositionEvent compositionEvent )
    {
        DeploymentModel model = compositionEvent.getChild();
        processModel( model, true );
    }

    /**
     * modelRemoved
     *
     * @param compositionEvent CompositionEvent
     */
    public void modelRemoved( CompositionEvent compositionEvent )
    {
        DeploymentModel model = compositionEvent.getChild();
        processModel( model, false );
    }

    private void processModel( DeploymentModel model, boolean flag )
    {
        if ( model instanceof ContainmentModel )
        {
            ContainmentModel containment = 
              (ContainmentModel) model;
            if ( flag )
            {
                containment.addCompositionListener( this );
            }
            else
            {
                containment.removeCompositionListener( this );
            }
            DeploymentModel[] models = containment.getModels();
            for ( int i = 0; i < models.length; i++ )
            {
                processModel( models[i], flag );
            }
        }
        else if ( model instanceof ComponentModel )
        {
            final ComponentModel componentModel = 
              (ComponentModel)model;

            if ( flag )
            {
                try
                {
                    m_manager.register( componentModel );
                }
                catch ( ComponentRegistrationException cme )
                {
                    final String message = 
                      REZ.getString( 
                        "compositionlistener.error.register",
                        componentModel.getQualifiedName() );
                    getLogger().debug( message, cme );
                }
            }
            else
            {
                try
                {

                    m_manager.unregister( componentModel );
                }
                catch ( ComponentRegistrationException cme )
                {
                    final String message = 
                      REZ.getString( 
                        "compositionlistener.error.unregister",
                        componentModel.getQualifiedName() );
                    getLogger().debug( message, cme );
                }
            }
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}
