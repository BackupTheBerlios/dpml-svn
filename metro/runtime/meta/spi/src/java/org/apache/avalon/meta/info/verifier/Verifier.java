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

package org.apache.avalon.meta.info.verifier;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.Service;

/**
 * The Verifier interface is used to allow service providers to ensure that
 * the meta information for the type or service is correct.  The validation
 * process may go so far as to ensure all the classes referenced actually
 * exist, although this is not necessary.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Verifier.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Verifier
{
    /**
     * Validate a type using the supplied Type information.
     *
     * @param type  The type we need to verify
     * @return <code>true</code> if it is valid
     */
    boolean isTypeValid( Type type );

    /**
     * Validate a service using the supplied Service information.
     *
     * @param service  The service we need to verify
     * @return <code>true</code> if it is valid
     */
    boolean isServiceValid( Service service );
}
