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

package org.apache.avalon.activation;

/**
 * Exception to indicate that there was a transient exception.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TransientApplianceException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class TransientApplianceException extends ApplianceException
{
     private final long m_delay;

    /**
     * Construct a new <code>TransientApplianceException</code> instance.
     *
     * @param delay the expected dalay
     */
    public TransientApplianceException( final long delay )
    {
        this( null, delay );
    }

    /**
     * Construct a new <code>TransientApplianceException</code> instance.
     *
     * @param message the exception message
     * @param delay the expected dalay
     */
    public TransientApplianceException( String message, long delay )
    {
        this( message, null, delay );
    }

    /**
     * Construct a new <code>TransientApplianceException</code> instance.
     *
     * @param message the exception message
     * @param cause the root cause of the exception
     * @param delay the projected delay
     */
    public TransientApplianceException( String message, Throwable cause, long delay )
    {
        super( message, cause );
        m_delay = delay;
    }

   /**
    * Return the projected delay.
    * @return the delay in milliseconds
    */
    public long getDelay()
    {
        return m_delay;
    }
}

