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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContainerImpl implements Container {

    public interface Context
    {
        public URI getConfigurationURI();
    }
    
    public interface Parts
    {
       public Child  getChild( boolean proxy );
    }
    
    private URI m_uri;
    private Child m_child;

    private Logger m_logger;
    private Context m_context;
    
    public ContainerImpl( final Logger logger, final Context context, final Parts parts )
    {
        m_logger  = logger;
        m_uri = context.getConfigurationURI();
        m_logger.info( "configuration uri: " + m_uri );
        m_child = parts.getChild( false );
    }
    
    public void start() throws Exception
    {
        m_logger.info( "starting" );
        m_child.start( m_uri );
        m_logger.info( "configurable service started successfully" );
    }

    public synchronized void stop() throws Exception 
    {
        m_logger.info( "stopping" );
        m_child.stop();
    }
}
