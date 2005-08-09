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

package net.dpml.profile;

import java.util.EventObject;

/**
 * An event pertaining to the addition or removal of an activation profile
 * from or to an activation group profile.
 */
public class ActivationGroupEvent extends EventObject 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    ActivationProfile m_profile;

    public ActivationGroupEvent( ActivationGroupProfile group, ActivationProfile profile )
    {
        super( group );
        m_profile = profile;
    }

    public ActivationGroupProfile getActivationGroupProfile()
    {
        return (ActivationGroupProfile) super.getSource();
    }

    public ActivationProfile getActivationProfile()
    {
        return m_profile;
    }

}
