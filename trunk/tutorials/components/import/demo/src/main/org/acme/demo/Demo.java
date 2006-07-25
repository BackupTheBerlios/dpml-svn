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

package org.acme.demo;

import org.acme.Clock;

import net.dpml.logging.Logger;

/**
 * A minimal component.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Demo
{
    //------------------------------------------------------------------
    // criteria
    //------------------------------------------------------------------
    
   /**
    * Internal parts accessor interface.
    */
    public interface Parts
    {
       /**
        * Get the clock instance.
        * @return a clock
        */
        Clock getClock();
    }
    
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------
    
    private final Logger m_logger;
    private final Parts m_parts;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------
    
   /**
    * Create a new demo instance.
    * @param logger the assigned logging channel
    * @param parts the internal parts
    */
    public Demo( final Logger logger, final Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
        Clock clock = parts.getClock();
        logger.info( 
          clock.getTimestamp() 
          + " (from " 
          + clock.getClass().getName() 
          + ")"
        );
    }
}
