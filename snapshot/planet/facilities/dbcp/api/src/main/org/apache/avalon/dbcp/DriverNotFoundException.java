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
 
package org.apache.avalon.dbcp;

/**
 * Thrown when determined that the user has not defined at least
 * the JDBC driver of the named datasource.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/11 21:23:02 $
 */
public class DriverNotFoundException extends Exception 
{

    /**
     * Constructs a new exception with <code>null</code> as the
     * detailed message.
     */
    public DriverNotFoundException()
    {
        super();
    }

    /**
     * Constructs a new exception with the specified detail
     * message.
     * 
     * @param message the detail message
     */
    public DriverNotFoundException( final String message )
    {
        super( message );
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * <p>The message will be (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of
     * cause). 
     * This constructor is useful for exceptions that are
     * little more than wrappers for other throwables.</p>
     *  
     * @param cause the cause
     */
    public DriverNotFoundException( final Throwable cause )
    {
        super( cause );
    }

    /**
     * Constructs a new exception with the specified detail message
     * and cause.
     * 
     * <p>Note that the detail message associated with cause is not
     * automatically incorporated in this exception's detail message.</p>
     *  
     * @param message the detail message
     * @param cause the cause
     */
    public DriverNotFoundException( final String message,
            final Throwable cause)
    {
        super( message, cause );
    }

}
