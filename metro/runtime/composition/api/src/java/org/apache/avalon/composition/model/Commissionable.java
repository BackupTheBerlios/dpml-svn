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

package org.apache.avalon.composition.model;

/**
 * The Comissionable interface defines the contract for an manager 
 * of deployable components. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Commissionable.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Commissionable
{
   /**
    * Commission the runtime handler. 
    *
    * @exception Exception if a hanfdler commissioning error occurs
    */
    void commission() throws Exception;

   /**
    * Invokes the decommissioning phase.  Once a handler is 
    * decommissioned it may be re-commissioned.
    */
    void decommission();

}
