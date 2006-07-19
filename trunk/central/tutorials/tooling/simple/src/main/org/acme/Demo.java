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

package org.acme;

import java.util.Date;
import java.util.logging.Logger;

/**
 * A minimal component.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Demo
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final Logger m_logger;
    
    //------------------------------------------------------------------
    // criteria
    //------------------------------------------------------------------
    
    public interface Context
    {
        Date getTimestamp( Date date );
    }
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
   /**
    * Creation of a new object using a supplied logging channel.
    * @param logger the logging channel
    */
    public Demo( final Logger logger, final Context context )
    {
        m_logger = logger;
        Date timestamp = context.getTimestamp( new Date() );
        m_logger.info( "TIMESTAMP: " + timestamp );
    }
}
