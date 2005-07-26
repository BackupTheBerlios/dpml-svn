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

package org.apache.avalon.composition.model.impl;

/**
 * The State class desribes a enabled versus disabled state.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultState.java 30977 2004-07-30 08:57:54Z niclas $
 */

class DefaultState
{

    //---------------------------------------------------------------------------
    // state
    //---------------------------------------------------------------------------

    private boolean m_enabled = false;

    //---------------------------------------------------------------------------
    // State
    //---------------------------------------------------------------------------

   /**
    * Return the enabled state of the state.
    * @return TRUE if the state has been enabled else FALSE
    */
    public boolean isEnabled()
    {
        return m_enabled;
    }

    //---------------------------------------------------------------------------
    // implementation
    //---------------------------------------------------------------------------

   /**
    * Set the enabled state of the state.
    * @param enabled the enabled state to assign
    */
    public void setEnabled( boolean enabled ) throws IllegalStateException
    {
        m_enabled = enabled;
    }
}