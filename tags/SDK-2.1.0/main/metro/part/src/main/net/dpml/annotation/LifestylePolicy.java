/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.annotation;

/**
 * Lifestyle policy.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public enum LifestylePolicy
{
   /**
    * Declares that a single instance created from the component class
    * shall be shared across all instance requests.  Components declaring 
    * this policy must ensure that implementations are threadsafe.
    */
    SINGLETON,
   
   /**
    * Declares that a new instance of the class shall be created for 
    * each thread context.
    */
    THREAD,
    
   /**
    * Declares that a new instance shall be created per request.
    */
    TRANSIENT;
}
