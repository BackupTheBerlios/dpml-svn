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

import org.apache.avalon.framework.service.ServiceException;

/**
 * Exception to indicate that there was a transient service error. The 
 * exception exposes a delay value which is the anticipated delay in 
 * service availability.  A delay value of 0 indicates an unknown delay.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TransientServiceException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class TransientServiceException
        extends ServiceException 
{
     private final long m_delay;

    /**
     * Construct a new <code>TransientServiceException</code> instance.
     *
     * @param key the lookup key
     * @param message The detail message for this exception.
     * @param delay expected service availability delay in milliseconds 
     */
    public TransientServiceException( 
      final String key, final String message, long delay )
    {
        super( key, message );
        m_delay = delay;
    }

   /**
    * Returns the expected duration of service non-availability.
    * @return the non-availability duration
    */
    public long getDelay()
    {
        return m_delay;
    }
}

