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

package org.apache.avalon.merlin.jmx;

import org.apache.avalon.merlin.KernelController;


/**
 * Management interface for the Merlin Kernel.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultKernelMBean.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface DefaultKernelMBean extends KernelController
{
    /**
     * Return an approximation to the total amount of memory currently 
     * available for future allocated objects, measured in bytes.
     * @return the number of bytes of estimated free memory
     */
    long getMemoryFree();

   /**
    * Returns the total amount of memory in the Java virtual machine. The value 
    * returned by this method may vary over time, depending on the host environment. 
    *
    * @return the total amount of memory currently available for current and 
    *   future objects, measured in bytes.
    */
    long getMemoryTotal();

   /**
    * Return the percentage of free memory available.
    * @return the free memory percentage
    */
    int getMemoryVariableRatio();

   /**
    * Return the number of active threads.
    * @return the active thread count
    */
    int getThreadCount();

   /**
    * Return the state of the kernel.
    * @return a string representing the kernel state
    */
    String getKernelState();

   /**
    * Return the root directory to the shared repository.
    * @return the avalon home root repository directory
    */
    String getRepositoryDirectory();

   /**
    * Return the root directory to the merlin installation
    * @return the merlin home directory
    */
    String getHomePath();

   /**
    * Return the root directory to the merlin system repository
    * @return the merlin system repository directory
    */
    String getSystemPath();

   /**
    * Return the root directory to the merlin configurations
    * @return the merlin configuration directory
    */
    String getConfigPath();

   /**
    * Return the url to the kernel confiuration
    * @return the kernel configuration url
    */
    String getKernelPath();

   /**
    * Return the working client directory.
    * @return the working directory
    */
    String getWorkingPath();

   /**
    * Return the temporary directory.
    * @return the temp directory
    */
    String getTempPath();

   /**
    * Return the context directory from which relative 
    * runtime home directories will be established for 
    * components referencing urn:avalon:home
    *
    * @return the working directory
    */
    String getContextPath();

   /**
    * Return the anchor directory to be used when resolving 
    * library declarations in classload specifications.
    *
    * @return the anchor directory
    */
    String getAnchorPath();

   /**
    * Return info generation policy.  If TRUE the parameters 
    * related to deployment will be listed on startup. 
    *
    * @return the info policy
    */
    boolean isInfoEnabled();

   /**
    * Return debug policy.  If TRUE all logging channels will be 
    * set to debug level (useful for debugging).
    *
    * @return the debug policy
    */
    boolean isDebugEnabled();
}
