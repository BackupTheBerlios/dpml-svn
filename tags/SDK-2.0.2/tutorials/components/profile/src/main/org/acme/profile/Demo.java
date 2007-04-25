/*
 * Copyright 2006 Stephen J. McConnell.
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

package org.acme.profile;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A component with a context
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Demo
{
    //------------------------------------------------------------------
    // context
    //------------------------------------------------------------------
    
   /**
    * Component driven context criteria.
    */
    public interface Context
    {
       /**
        * Return an optional first name.
        * @param value the default value
        * @return the first name
        */
        String getFirstName( String value );
        
       /**
        * Return an optional surname.
        * @return the surname
        */
        String getLastName( String value );
    }
    
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final String m_firstName;
    private final String m_lastName;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new object using a supplied logging channel.
    * @param logger the logging channel
    * @param context the deployment context
    * @exception Exception if an error occurs
    */
    public Demo( final Logger logger, final Context context ) throws Exception
    {
        m_firstName = context.getFirstName( null );
        m_lastName = context.getLastName( null );
        
        if( logger.isLoggable( Level.INFO ) )
        {
            logger.info( "NAME: " + m_firstName + " " + m_lastName );
        }
    }
    
    public String getFirstName()
    {
        return m_firstName;
    }
    
    public String getLastName()
    {
        return m_lastName;
    }
}
