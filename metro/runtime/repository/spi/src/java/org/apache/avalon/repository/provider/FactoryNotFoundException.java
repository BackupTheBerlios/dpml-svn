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

package org.apache.avalon.repository.provider;

import org.apache.avalon.repository.RepositoryException;

/**
 * Exception to indicate that a requested factory was not found.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FactoryNotFoundException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class FactoryNotFoundException extends RepositoryException
{
    /**
     * Construct a new <code>FactoryNotFoundException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public FactoryNotFoundException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>FactoryNotFoundException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public FactoryNotFoundException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}

