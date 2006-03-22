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
 * Interface implemented by commissioner controllers.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CommissionerController
{
   /**
    * Notification that a commissioning or decommissioning 
    * process has commenced.
    * @param event the commissioner event
    */
    void started( CommissionerEvent event );
    
   /**
    * Notification that a commissioning or decommissioning 
    * process has successfully completed.
    * @param event the commissioner event
    */
    void completed( CommissionerEvent event );

   /**
    * Notification that a commissioning or decommissioning 
    * process has been interrupted.
    * @param event the commissioner event
    * @exception TimeoutException optional controller initiated exception
    */
    void interrupted( CommissionerEvent event ) throws TimeoutException;

   /**
    * Notification that a commissioning or decommissioning 
    * process has failed due to a timeout and the target commissionable 
    * object failed to resopond to an interrup and was subsequently 
    * terminated.
    * @param event the commissioner event
    * @param cause the causal exception
    * @exception InvocationTargetException optional wrapped client exception
    */
    void failed( CommissionerEvent event, Throwable cause ) throws InvocationTargetException;

   /**
    * Notification that a commissioning or decommissioning 
    * process has failed.
    * @param event the commissioner event
    * @exception TimeoutError optional controller initiated timeout error
    */
    void terminated( CommissionerEvent event ) throws TimeoutError;
}
