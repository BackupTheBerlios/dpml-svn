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

import net.dpml.lang.StandardClassLoader;

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
    
    public interface Parts
    {
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
    
    public Demo( final Logger logger, final Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
        Clock clock = parts.getClock();
        logger.info( 
          clock.getTimestamp() 
          + " (from " + clock.getClass().getName() 
          + ")"
        );
        list();
    }
    
    public void list()
    {
        m_logger.info( "A #############" );
        ClassLoader classloader = getClass().getClassLoader();
        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader loader = (StandardClassLoader) classloader;
            m_logger.info( loader.toString( true ) );
        }
        else
        {
            m_logger.info( classloader.toString() );
        }
        m_logger.info( "B #############" );
        m_parts.getClock().list();
        m_logger.info( "C #############" );
    }
}
