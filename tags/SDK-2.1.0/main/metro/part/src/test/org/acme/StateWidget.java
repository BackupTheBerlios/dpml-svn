/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

/**
 * Sample component that has an associated lifecycle graph.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component
@Services( Widget.class )
public class StateWidget implements Widget
{
    private final Logger m_logger;
    
    private int m_state = 0;
    
   /**
    * Component constructor.
    * @param logger the assigned logging channel
    */
    public StateWidget( Logger logger )
    {
        m_logger = logger;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "instantiated" );
        }
    }
    
   /**
    * Start the component.  This method is declared as an innitialization 
    * operation in the lifecycle graph and will be automatically invoked 
    * by the container as a part of the normal deployment process.
    * @exception Exception if something goes wrong
    */
    public void start() throws Exception
    {
        m_state = 1;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "starting" );
        }
    }
    
   /**
    * Get a message.
    * @return a message reflecting the runtime state.
    */
    public String getMessage()
    {
        if( m_state == 0 )
        {
            return "constructed";
        }
        else if( m_state == 1 )
        {
            return "started";
        }
        else if( m_state == 2 )
        {
            return "stopped";
        }
        else
        {
            return "unknown";
        }
    }

   /**
    * Get the runtime state.
    * @return the status of the component
    */
    public int getState()
    {
        return m_state;
    }
    
   /**
    * Stop the component.  This method is declared as a termination 
    * operation in the lifecycle graph and will be automatically invoked 
    * by the container as a part of the normal decommissioning process.
    * @exception Exception if something goes wrong
    */   
    public void stop() throws Exception
    {
        m_state = 2;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "stopping" );
        }
    }

   /**
    * Test the supplied object for equality with this object.
    * @param other the supplied object 
    * @return the equality result
    */
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }

   /**
    * Get the component hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        return m_state;
    }
}
