/*
 * Copyright 2005 Stephen McConnell
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

import dpml.util.PID;

import java.net.URI;

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import net.dpml.util.Logger;

/**
 * Sample component used in testing.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( lifestyle=SINGLETON )
@Services( Hello.class )
public class HelloComponent implements Hello
{
    private static final PID ID = new PID();
    private static final String MESSAGE = "Hello";
    
    private final Logger m_logger;
    private final HelloConfiguration m_context;
    
    public HelloComponent( Logger logger, HelloConfiguration context ) throws Exception
    {
        m_logger = logger;
        m_context = context;

        URI codebase = 
          getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        
        m_logger.info( "pid: " + ID );
        m_logger.info( "message: " + context.getMessage( MESSAGE ) );
        m_logger.info( "port: " + context.getPort( 0 ) );
        m_logger.info( "target: " + context.getTarget().getUri( codebase ) );
    }
    
    public String getMessage()
    {
        return m_context.getMessage( "Hello" );
    }
    
    public void start()
    {
        m_logger.info( "starting" );
        m_logger.info( "started" );
    }
    
    public void stop()
    {
        m_logger.info( "stopping" );
        m_logger.info( "stopped" );
    }
}
