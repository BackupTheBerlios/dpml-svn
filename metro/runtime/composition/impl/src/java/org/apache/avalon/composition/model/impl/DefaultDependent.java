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

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.Dependent;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * Default dependent model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultDependent.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultDependent extends AbstractLogEnabled implements Dependent
{
    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private DeploymentModel m_provider;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new stage model.
    *
    * @param logger the logging channel
    */
    public DefaultDependent( final Logger logger )
    {
        if( logger == null )
        {
            throw new NullPointerException( "logger" );
        }
        enableLogging( logger );
    }

    //--------------------------------------------------------------
    // Dependent
    //--------------------------------------------------------------

   /**
    * Set the provider model.
    * 
    * @param model the provider model
    */
    public void setProvider( DeploymentModel model )
    {
        m_provider = model;
    }

   /**
    * Return the assigned provider model.
    * 
    * @return the stage provider model
    */
    public DeploymentModel getProvider()
    {
        return m_provider;
    }

   /**
    * Clear the assigned provider.
    */
    public void clearProvider()
    {
        m_provider = null;
    }

}
