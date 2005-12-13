/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.test.app;

import net.dpml.logging.Logger;

/**
 * The demo class is used to aggregate a collection of components and 
 * provide some hooks for the testcase.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class Demo
{
   /**
    * The parts interface is used within the implementation to 
    * access subsystem.
    */
    public interface Parts
    {
       /**
        * Return the server implementation.
        * @return the server
        */
        DefaultServer getServer();
        
       /**
        * Return the listener implementation.
        * @return the listener
        */
        DefaultListener getListener();
    }
    
    private final Parts m_parts;
    private final Logger m_logger;
    
   /**
    * Creation of a new demo component.
    * @param logger a logging channel
    * @param parts the internal parts
    */
    public Demo( Logger logger, Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
    }
    
   /**
    * Hook for testcase to fire a message noytify request to the 
    * server which in turn fires notifiy requests to listeners.
    * @param message the notification message
    * @return the number of notify messages fired to the listener
    */
    public int test( String message )
    {
        m_logger.debug( "test: " + message );
        m_parts.getServer().triggerNotify( message );
        return m_parts.getListener().getCount();
    }
}
