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

package org.apache.avalon.merlin.event;

import java.util.EventObject;

import org.apache.avalon.merlin.Kernel;

/**
 * A event raised by a kernel signaling a state change.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: KernelEvent.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class KernelEvent extends EventObject
{
    /**
     * The model added or removed from the containment model.
     */
    private final Kernel m_kernel;

    /**
     * Create a CompositionEvent event.
     *
     * @param kernel the kernel instance raising the event
     */
    public KernelEvent( final Kernel kernel )
    {
        super( kernel );
        m_kernel = kernel;
    }

    /**
     * Return the kernel raising the event.
     *
     * @return the source kernel
     */
    public Kernel getKernel()
    {
        return m_kernel;
    }

    public String toString()
    {
        return "kernel-event: [" 
          + getKernel() 
          + "]";
    } 
}
