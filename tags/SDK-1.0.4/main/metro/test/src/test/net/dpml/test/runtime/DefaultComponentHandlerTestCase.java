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

package net.dpml.test.runtime;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;


/**
 * Contains a series of tests applied to the component component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultComponentHandlerTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Testcase setup during which the part defintion 'example.part'
    * is established as a file uri.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test that the component initial state is inactive.
    * @exception Exception if an unexpected error occurs
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
    * @exception Exception if an unexpected error occurs
    */
    public void testActivationDeactivationCycle() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
        assertTrue( "is-active", component.isActive() );
        component.decommission();
        assertFalse( "is-active-following-deactivation", component.isActive() );
    }

   /**
    * Test self activation on access.
    * @exception Exception if an unexpected error occurs
    */
    public void testProviderAquisitionInInactiveState() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Provider instance = (Provider) component.getProvider();
        assertTrue( "is-active-post-instantiation", component.isActive() );
    }
}
