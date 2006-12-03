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

package org.acme.impl;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.acme.Clock;

/**
 * A minimal implementation of a clock.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SimpleClock implements Clock
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final String m_format; 
    private final Locale m_locale; 
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Creation of a new instance.
    * @param format the format to use for timestamps
    * @param locale the assigned locale
    */
    public SimpleClock( final String format, final Locale locale )
    {
        m_format = format;
        m_locale = locale;
    }
    
    //------------------------------------------------------------------
    // Clock
    //------------------------------------------------------------------
    
   /**
    * Return the current time as a formatted string.
    * @return the current time as a string
    */
    public String getTimestamp()
    {
        Date date = new Date();
        DateFormat formatter =  new SimpleDateFormat( m_format, m_locale );
        return formatter.format( date );
    }
    
}
