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

import org.apache.avalon.activation.RuntimeFactory;

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.Runtime;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Implementation of a system context that exposes a system wide set of parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultRuntime.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultRuntime implements Runtime
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultRuntime.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final RuntimeFactory m_runtime;

    //--------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------

    private boolean m_trace;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new system context.
    *
    * @param system the system context
    */
    public DefaultRuntime( SystemContext system )
    {
        if( system == null )
        {
            throw new NullPointerException( "system" );
        }
        m_runtime = new DefaultRuntimeFactory( system );
    }

    //--------------------------------------------------------------
    // Runtime
    //--------------------------------------------------------------

   /**
    * Request the commissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    public void commission( DeploymentModel model ) throws Exception
    {
        getRuntimeFactory().getRuntime( model ).commission();
    }

   /**
    * Request the decommissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    public void decommission( DeploymentModel model )
    {
        getRuntimeFactory().getRuntime( model ).decommission();
    }

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @exception Exception if a deployment error occurs
    */
    public Object resolve( DeploymentModel model ) throws Exception
    {
        return getRuntimeFactory().getRuntime( model ).resolve();
    }

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @param proxy if TRUE the return value will be proxied if the 
    *   underlying component typoe suppports proxy representation 
    * @exception Exception if a deployment error occurs
    */
    public Object resolve( DeploymentModel model, boolean proxy ) throws Exception
    {
        return getRuntimeFactory().getRuntime( model ).resolve( proxy );
    }

   /**
    * Request the release of an object from the runtime.
    * @param model the deployment model
    * @param instance the object to release
    * @exception Exception if a deployment error occurs
    */
    public void release( DeploymentModel model, Object instance )
    {
        getRuntimeFactory().getRuntime( model ).release( instance );
    }

    //------------------------------------------------------------------
    // runtime operations
    //------------------------------------------------------------------

   /**
    * Return the runtime factory.
    *
    * @return the factory
    */
    private RuntimeFactory getRuntimeFactory()
    {
        return m_runtime;
    }


}
