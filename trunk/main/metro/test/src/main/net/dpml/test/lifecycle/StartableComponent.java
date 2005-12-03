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

package net.dpml.test.lifecycle;

import net.dpml.logging.Logger;

/**
 * Test component that provides feedback on the application of lifecycle
 * stages handled by the container.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class StartableComponent
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;
    
    private boolean m_started = false;
    private boolean m_stopped = false;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new startable component instance.
    * @param logger the assingned logging channel
    */
    public StartableComponent( final Logger logger )
    {
        m_logger = logger;
    }
    
    //------------------------------------------------------------------
    // lifecycle
    //------------------------------------------------------------------

   /**
    * Start the component.
    */
    public void start()
    {
        m_started = true;
    }
    
   /**
    * Stop the component.
    */
    public void stop()
    {
        m_stopped = true;
    }

    //------------------------------------------------------------------
    // validation operations
    //------------------------------------------------------------------
    
    public boolean wasStarted()
    {
        return m_started;
    }
    
    public boolean wasStopped()
    {
        return m_stopped;
    }
}
