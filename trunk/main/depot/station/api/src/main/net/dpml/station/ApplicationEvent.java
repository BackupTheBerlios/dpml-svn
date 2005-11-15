/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.station;

import java.util.EventObject;

import net.dpml.station.ProcessState;

/**
 * An application registry event.
 */
public class ApplicationEvent extends EventObject 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final ProcessState m_state;

   /**
    * Creation of a new application event.
    * @param application event source
    * @param state the state established by the application
    */
    public ApplicationEvent( Application application, ProcessState state )
    {
        super( application );

        m_state = state;
    }

   /**
    * Return the application that initiated the event.
    * @return application event source
    */
    public Application getApplication()
    {
        return (Application) super.getSource();
    }

   /**
    * Return the state associated with the event.
    * @return the application state
    */
    public ProcessState getState()
    {
        return m_state;
    }
}
