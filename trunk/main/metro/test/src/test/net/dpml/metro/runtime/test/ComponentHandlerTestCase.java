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
import net.dpml.part.Instance;


/**
 * Contains a series of tests applied to the component component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentHandlerTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test that the component initial state is inactive.
    */
    public void testHandlerInitialState() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        assertNotNull( "component", component );
        assertFalse( "initial-active-state", component.isActive() );
    }
    
   /**
    * Test that the component exposes itself as active following activation 
    * and inactive following deactivation.
    */
    public void testActivationDeactivationCycle() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        assertTrue( "is-active", component.isActive() );
        component.deactivate();
        assertFalse( "is-active-following-deactivation", component.isActive() );
    }

   /**
    * Test that an IllegalStateException is thrown if a client attempts to 
    * access an Instance from an inactive component.
    */
    public void testInstanceAquisitionInInactiveState() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Instance instance = (Instance) component.getInstance();
        assertTrue( "is-active-post-instantiation", component.isActive() );
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
