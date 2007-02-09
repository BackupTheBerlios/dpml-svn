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

import java.awt.Color;

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.TRANSIENT;

import net.dpml.util.Logger;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component
@Services( Widget.class )
public class StateWidget implements Widget
{
    private final Logger m_logger;
    
    private int m_state = 0;
    
    public StateWidget( Logger logger )
    {
        m_logger = logger;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "instantiated" );
        }
    }
    
    public void start() throws Exception
    {
        m_state = 1;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "starting" );
        }
    }
    
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

    public int getState()
    {
        return m_state;
    }
    
    public void stop() throws Exception
    {
        m_state = 2;
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "stopping" );
        }
    }

    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}
