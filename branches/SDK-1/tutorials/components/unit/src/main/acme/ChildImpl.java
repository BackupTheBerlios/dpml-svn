/*
 * Copyright 2006 Juan Pablo Rojas Jiménez.
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
package acme;

import java.net.URI;
import java.util.logging.Logger;

public class ChildImpl implements Child 
{
    private boolean m_started;
    private Logger  m_logger;
    
    public ChildImpl( final Logger logger )
    {
        m_started = false;
        m_logger  = logger;
    }
    
    public void start( final URI uri ) throws Exception 
    {
        if( !m_started )
        {
            m_started = true;
            m_logger.info( "starting with: " + uri );
        } 
        else
        {
            throw new Exception( "already started!" );
        }
    }

    public void stop() throws Exception 
    {
        if ( m_started ){
            m_started = false;
            m_logger.info( "stopping" );
        } 
        else
        {
            throw new Exception( "Not started!" );
        }
    }
}
