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

import java.util.List;

import java.security.AccessControlContext;

import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.AssemblyException;
import org.apache.avalon.composition.model.Commissionable;

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.data.CategoriesDirective;


public class SimpleDeploymentModel 
    implements DeploymentModel
{
    private long m_timeout;
    private Commissionable m_commissionable;

    SimpleDeploymentModel( Commissionable c, long timeout )
    {
        m_timeout = timeout;
        m_commissionable = c;
    }
    
   /**
    * Return the name of the model.
    * @return the name
    */
    public String getName()
    {
        return "dummy";
    }

   /**
    * Return the model partition path.
    * @return the path
    */
    public String getPath()
    {
        return "/dummy";
    }

   /**
    * Return the model fully qualified name.
    * @return the fully qualified name
    */
    public String getQualifiedName()
    {
        return "/dummy";
    }

   /**
    * Return the mode of model establishment.
    * @return the mode
    */
    public Mode getMode()
    {
        return null;
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    public Logger getLogger()
    {
        return null;
    }

   /**
    * Return the logging categories. 
    * @return the logging categories
    */
    public CategoriesDirective getCategories()
    {
        return null;
    }

   /**
    * Set categories. 
    * @param categories the logging categories
    */
    public void setCategories( CategoriesDirective categories ) 
    {
    }

    public AccessControlContext getAccessControlContext()
    {
        return null;
    }

    //-----------------------------------------------------------
    // service production
    //-----------------------------------------------------------
    
   /**
    * Return the set of services produced by the model.
    * @return the services
    */
    public ServiceDescriptor[] getServices()
    {
        return new ServiceDescriptor[0];
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * depedendency.
    * @return true if this model can fulfill the dependency
    */
    public boolean isaCandidate( DependencyDescriptor dependency )
    {
        return true;
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * stage dependency.
    * @return true if this model can fulfill the dependency
    */
    public boolean isaCandidate( StageDescriptor stage )
    {
        return true;
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * service.
    * @return true if this model can fulfill the service
    */
    public boolean isaCandidate( ReferenceDescriptor reference )
    {
        return true;
    }


    //-----------------------------------------------------------
    // composite assembly
    //-----------------------------------------------------------

   /**
    * Returns the assembled state of the model.
    * @return true if this model is assembled
    */
    public boolean isAssembled()
    {
        return true;
    }

    /**
     * Assemble the model.
     * @exception Exception if an error occurs during model assembly
     */
    public void assemble( List subjects ) throws AssemblyException
    {
    }

   /**
    * Return the set of models consuming this model.
    * @return the consumers
    */
    public DeploymentModel[] getConsumerGraph()
    {
        return null;
    }

   /**
    * Return the set of models supplying this model.
    * @return the providers
    */
    public DeploymentModel[] getProviderGraph()
    {
        return null;
    }

   /**
    * Disassemble the model.
    */
    public void disassemble(){}

   /**
    * Return the set of models assigned as providers.
    * @return the providers consumed by the model
    * @exception IllegalStateException if invoked prior to 
    *    the completion of the assembly phase 
    */
    public DeploymentModel[] getProviders()
    {
        return null;
    }

   /** 
    * Returns the maximum allowable time for deployment.
    *
    * @return the maximum time expressed in millisecond of how 
    * long a deployment may take.
    */
    public long getDeploymentTimeout()
    {
        return m_timeout;
    }

    public Object resolve() throws Exception{ return null; }
    public Object resolve( boolean proxy ) throws Exception{ return null; }
    public void release( Object object ){};

    public void commission() throws Exception
    {
        m_commissionable.commission();
    }

    public void decommission(){};

}
