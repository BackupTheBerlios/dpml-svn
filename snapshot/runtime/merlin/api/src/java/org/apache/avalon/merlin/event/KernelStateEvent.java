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

import org.apache.avalon.merlin.Kernel;

/**
 * A event raised by a kernel signaling a state change.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: KernelStateEvent.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class KernelStateEvent extends KernelEvent
{
    private final int m_from;
    private final int m_to;

    /**
     * Create a CompositionEvent event.
     *
     * @param kernel the kernel instance raising the event
     * @param from the old state value
     * @param to the new state value
     */
    public KernelStateEvent( final Kernel kernel, int from, int to )
    {
        super( kernel );
        m_from = from;
        m_to = to;
    }

    /**
     * Return the initial state before the transition.
     *
     * @return the source kernel
     */
    public int getInitialState()
    {
        return m_from;
    }

    /**
     * Return the state by the transition.
     *
     * @return the source kernel
     */
    public int getCurrentState()
    {
        return m_to;
    }

    public String toString()
    {
        return "kernel-event: [" 
          + getKernel() 
          + " (" + getInitialState() 
          + "/" + getCurrentState() 
          + ")]";
    } 
}
