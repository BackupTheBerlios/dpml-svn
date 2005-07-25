/* 
 * Copyright 2004 Stephen McConnell, DPML
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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Transit authenticator contract.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TransitAuthenticator.java 2143 2005-03-26 18:22:09Z niclas@hedhman.org $
 */
public interface TransitAuthenticator
{
    /**
     * Returns the password authenticator.
     * @param authenticator the default authenticator
     * @return the password authenticator
     */
    PasswordAuthentication resolvePasswordAuthentication( Authenticator authenticator );
}
