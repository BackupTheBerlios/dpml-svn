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
package org.apache.avalon.jmx;

import org.apache.avalon.framework.CascadingException;

/**
 * An exception indicating a problem performing component management.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
public class ComponentRegistrationException extends CascadingException
{
    /**
     * Constructs a new ComponentRegistrationException with the specified message.
     *
     * @param message a description of the cause of this ComponentRegistrationException
     */
    public ComponentRegistrationException( String message )
    {
        super( message );
    }

    /**
     * Constructs a new ComponentRegistrationException with the specified message and cause.
     *
     * @param message a description of the cause of this ComponentRegistrationException
     * @param cause the cause of this ComponentRegistrationException
     */
    public ComponentRegistrationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
