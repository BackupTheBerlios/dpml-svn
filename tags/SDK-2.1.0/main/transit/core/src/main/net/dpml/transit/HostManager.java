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

package net.dpml.transit;

import javax.management.MXBean;
import javax.management.MBeanException;

/** 
 * Management interface to a resource host.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
 @MXBean
public interface HostManager
{
   /**
    * Returns the host identifier.
    * @return the host id
    * @exception MBeanException if a JMX error occurs
    */
    String getID() throws MBeanException;
    
   /**
    * Returns the host priority.
    * @return the host priority value
    * @exception MBeanException if a JMX error occurs
    */
    int getPriority() throws MBeanException;
    
   /**
    * Returns the host base url.
    * @return the host url
    * @exception MBeanException if a JMX error occurs
    */
    String getBase() throws MBeanException;
        
   /**
    * Returns the host layout strategy identifier
    * @return the layout identifier
    * @exception MBeanException if a JMX error occurs
    */
    String getLayoutID() throws MBeanException;
    
   /**
    * Returns the host enabled status.
    * @return the enabled state
    * @exception MBeanException if a JMX error occurs
    */
    boolean isEnabled() throws MBeanException;
    
   /**
    * Returns the host trusted status.
    * @return the trusted state
    * @exception MBeanException if a JMX error occurs
    */
    boolean isTrusted() throws MBeanException;
    
}