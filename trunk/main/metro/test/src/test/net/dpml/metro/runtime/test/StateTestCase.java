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

package net.dpml.metro.runtime.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.Provider;
import net.dpml.part.Context;

import net.dpml.test.state.Service;
import net.dpml.test.state.ManagedComponent;
import net.dpml.test.state.ManagedComponent.Monitor;

/**
 * Contains a series of tests dealing with a composite application.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StateTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
    public void setUp() throws Exception
    {
        final String path = "state.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Validate composite instantiation and in particular that the color
    * assigned to the child component has been overriden by the parent. 
    */
    public void testComponent() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Provider provider = component.getProvider();
        Service service = (Service) provider.getValue( true );
        for( int i=0; i<100; i++ )
        {
            service.ping();
        }
        Monitor monitor = (Monitor) provider.exec( "monitor", new Object[0] );
        assertEquals( "count", 100, monitor.getAccessCount() );
    }
    
    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
