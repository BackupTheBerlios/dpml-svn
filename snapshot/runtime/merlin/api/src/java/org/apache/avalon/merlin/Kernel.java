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

package org.apache.avalon.merlin;

import org.apache.avalon.merlin.event.KernelEventListener;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;

/**
 * A Kernel is the root of a containment solution. This interfaces 
 * defines the contract for any kernel implementation covering 
 * management aspects and service resolution aspects.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Kernel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Kernel extends KernelController
{
    static final int INITIALIZING = 0;
    static final int INITIALIZED = 1;
    static final int STARTING = 2;
    static final int ASSEMBLY = 3;
    static final int DEPLOYMENT = 4;
    static final int STARTED = 5;
    static final int STOPPING = 6;
    static final int DECOMMISSIONING = 7;
    static final int DISSASSEMBLY = 8;
    static final int STOPPED = 9;

   /**
    * Add a kernel listener.
    * @param listener the kernel listener to be added
    */
    void addKernelEventListener( KernelEventListener listener );

   /**
    * Remove a kernel listener.
    * @param listener the kernel listener to be removed
    */
    void removeKernelEventListener( KernelEventListener listener );

   /**
    * Return the root containment model.
    * @return the root model
    */
    ContainmentModel getModel();

   /**
    * Return a model matching the supplied path.
    * @return the model
    */
    DeploymentModel locate( String path );

   /**
    * Return the current state of the kernel.
    * @return the kernel state
    */
    int getState();

}
