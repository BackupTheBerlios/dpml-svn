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

package net.dpml.profile.model;

import java.util.EventObject;

/**
 * An application registry event.
 */
public class ApplicationProfileEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Creation of a new application profile event instance.
    * @param profile the application profile raising the event
    */
    public ApplicationProfileEvent( ApplicationProfile profile )
    {
        super( profile );
    }

   /**
    * Return the source application profile.
    * @return the application profile raising the event
    */
    public ApplicationProfile getApplicationProfile()
    {
        return (ApplicationProfile) super.getSource();
    }
}
