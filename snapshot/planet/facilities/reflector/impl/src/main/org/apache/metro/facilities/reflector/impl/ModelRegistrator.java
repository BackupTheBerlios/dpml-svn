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

package org.apache.metro.facilities.reflector.impl;

import org.apache.avalon.framework.activity.Initializable;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.composition.event.CompositionEvent;
import org.apache.avalon.composition.event.CompositionListener;

import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.metro.facilities.reflector.ReflectorService;

/** This component will lookup the composition model and register
 *
 * @avalon.component name="model-registrator" lifestyle="singleton"
 *                   collection="weak"
 */
public class ModelRegistrator
    implements Contextualizable, Serviceable, Initializable, 
               CompositionListener
{
    private ContainmentModel m_Model;
    private ReflectorService m_Reflector;
    
    /**
     * Contextulaization of the listener by the container during 
     * which we are supplied with the root composition model for 
     * the application.
     *
     * @param ctx the supplied listener context
     *
     * @exception ContextException if a contextualization error occurs
     *
     * @avalon.entry key="urn:composition:containment.model" 
     *               type="org.apache.avalon.composition.model.ContainmentModel" 
     */
    public void contextualize( Context ctx ) 
        throws ContextException
    {
        m_Model = (ContainmentModel) ctx.get( "urn:composition:containment.model" );
        m_Model = (ContainmentModel) m_Model.getModel("/");
        m_Model.addCompositionListener( this );
    }

    /** The service method is called by the Avalon framework container.
     * <p>It is imoprtant that this method is not called directly.</p>
     *
     * @avalon.dependency type="org.apache.metro.facilities.reflector.ReflectorService"
     *                   key="reflector"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_Reflector = (ReflectorService) man.lookup( "reflector" );
    }    
    
    public void initialize()
        throws Exception
    {
        m_Reflector.addRootObject( "model", m_Model );
    }

    /**
     * Notify the listener that a model has been added to 
     * a source containment model.
     *
     * @param event the containment event raised by the 
     *    source containment model
     */
    public void modelAdded( CompositionEvent event )
    {
    }

    /**
     * Notify the listener that a model has been removed from 
     * a source containment model.
     *
     * @param event the containment event raised by the 
     *    source containment model
     */
    public void modelRemoved( CompositionEvent event )
    {
    }

}
