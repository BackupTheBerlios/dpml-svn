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

package org.apache.avalon.merlin.impl;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.merlin.KernelContext;

/**
 * The context argument supplied to a new kernel instance.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultKernelContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultKernelContext implements KernelContext
{
    private final Logger m_logger;
    private final ContainmentModel m_application;

   /**
    * Creation of a new default kernel context.
    * @param logger the logging channel to be assigned to the kernel
    * @param application the application model
    */
    public DefaultKernelContext( 
      final Logger logger, final ContainmentModel application )
    {
        m_logger = logger;
        m_application = application;
    }
    
   /**
    * Return the assigned logging channel.
    * @return the loggging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the application model.
    * @return the root application model 
    */
    public ContainmentModel getApplicationModel()
    {
        return m_application;
    }
}
