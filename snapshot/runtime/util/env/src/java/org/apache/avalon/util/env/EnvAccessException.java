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

package org.apache.avalon.util.env ;

import java.io.IOException;

/**
 * A simple wrapper exception around exceptions that could occur while accessing
 * environment parameters.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: EnvAccessException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class EnvAccessException extends IOException
{
    /** the environment variable name if available */
    public final String m_variable ;
    /** root cause */
    public final Throwable m_cause ;
    
    /**
     * Creates an exception denoting a failure while attempting to access an 
     * environment variable within an operating system and shell specific 
     * environment that is caused by another exception.
     * 
     * @param a_cause the underlying exception that caused the failure
     */
    EnvAccessException( final Throwable a_cause )
    {
        super() ;
        
        m_variable = null ;
        m_cause = a_cause ;
    }


    /**
     * Creates an exception denoting a failure while attempting to access an 
     * environment variable within an operating system and shell specific 
     * environment.
     * 
     * @param a_message the reason for the access failure 
     */
    EnvAccessException( final String a_message )
    {
        super( a_message ) ;
        
        m_variable = null ;
        m_cause = null ;
    }

    
    /**
     * Creates an exception denoting a failure while attempting to access an 
     * environment variable within an operating system and shell specific 
     * environment that is caused by another exception.
     * 
     * @param a_variable the variable whose value was to be accessed
     * @param a_cause the underlying exception that caused the failure
     */
    EnvAccessException( final String a_variable, final Throwable a_cause )
    {
        super() ;
        
        m_variable = a_variable ;
        m_cause = a_cause ;
    }


    /**
     * Creates an exception denoting a failure while attempting to access an 
     * environment variable within an operating system and shell specific 
     * environment.
     * 
     * @param a_variable the variable whose value was to be accessed
     * @param a_message the reason for the access failure 
     */
    EnvAccessException( final String a_variable, final String a_message )
    {
        super( a_message ) ;
        
        m_variable = a_variable ;
        m_cause = null;
    }


    /**
     * Gets the variable that was to be accessed.
     * 
     * @return the value of the variable 
     */
    public String getVariable()
    {
        return m_variable ;
    }

    
    /**
     * Return the causal exception.
     * 
     * @return the exception that caused this exception (possibly null)
     */
    public Throwable getCause()
    {
        return m_cause ;
    }

    
    /**
     * Prepends variable name to the base message.
     * 
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage()
    {
        String l_base = super.getMessage() ;
        
        if ( null == l_base )
        {    
            return "Failed to access " + m_variable + " environment variable" ;
        }
        
        return "Failed to access " + m_variable 
            + " environment variable - " + l_base ;
    }
}



