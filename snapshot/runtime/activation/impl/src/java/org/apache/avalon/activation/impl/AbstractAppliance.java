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

package org.apache.avalon.activation.impl;

import org.apache.avalon.activation.Appliance;

import org.apache.avalon.composition.model.DeploymentModel;

import org.apache.avalon.framework.logger.Logger;

/**
 * Abstract appliance.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractAppliance.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class AbstractAppliance implements Appliance
{
    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final DeploymentModel m_model;

    private final Logger m_logger;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public AbstractAppliance( DeploymentModel model )
    {
        if( null == model ) 
        {
            throw new NullPointerException( "model" );
        }

        m_model = model;
        m_logger = model.getLogger();
    }

    //-------------------------------------------------------------------
    // Commissionable
    //-------------------------------------------------------------------

   /**
    * Commission the appliance. 
    *
    * @exception Exception if a commissioning error occurs
    */
    public abstract void commission() throws Exception;

   /**
    * Decommission the appliance.  Once an appliance is 
    * decommissioned it may be re-commissioned.
    */
    public abstract void decommission();

    //-------------------------------------------------------------------
    // Resolver
    //-------------------------------------------------------------------

    /**
     * Resolve a object to a value.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    public abstract Object resolve() throws Exception;

   /**
    * Request resolution of an object from the runtime.
    * @param proxy if TRUE the return value will be proxied if the 
    *   underlying component typoe suppports proxy representation 
    * @exception Exception if a deployment error occurs
    */
    public abstract Object resolve( boolean proxy ) throws Exception;

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    public abstract void release( Object instance );

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Return the model backing the handler.
     * @return the type that the appliance is managing
     */
    protected DeploymentModel getDeploymentModel()
    {
        return m_model;
    }

    //-------------------------------------------------------------------
    // Object
    //-------------------------------------------------------------------

    public String toString()
    {
        return "appliance:" + getDeploymentModel().getQualifiedName();
    }
}
