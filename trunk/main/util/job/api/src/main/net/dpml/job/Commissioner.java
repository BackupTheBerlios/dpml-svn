/*
 * Copyright 2006 Stephen J. McConnell.
 *
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

package net.dpml.job;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface implemented by systems that handle ordered commissioning 
 * (or decommissioning) of a of commissionable objects.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Commissioner
{
    /** 
     * Commissions the given Commissonable, and allows a maximum time
     * for commissioning/decommissioning to complete.
     *
     * @param commissionable the commissionable instance
     * @param timeout the maximum number of milliseconds to allow 
     *
     * @throws TimeoutException if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was successful
     * @throws TimeoutError if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was not successful
     * @throws InvocationTargetException if the deployment target raises an error.
     **/
    void add( Commissionable commissionable, long timeout ) 
      throws TimeoutException, TimeoutError, InvocationTargetException;
}
