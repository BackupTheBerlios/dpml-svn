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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;
import net.dpml.metro.part.Part;
import net.dpml.metro.part.Directive;
import net.dpml.metro.part.Controller;
import net.dpml.metro.part.ActivationPolicy;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.ControlException;
import net.dpml.metro.state.State;
import net.dpml.metro.state.StateListener;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.impl.DefaultStateListener;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * Contains a series of tests applied to the component component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentHandlerTestCase extends TestCase
{    
    private ComponentModel m_model;
    private Controller m_control;
    private State m_state;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_control = Part.CONTROLLER;
        Directive directive = m_control.loadDirective( uri );
        m_model = (ComponentModel) m_control.createContext( directive );
    }
    
   /**
    * Test that the component initial state is inactive.
    */
    public void testHandlerInitialState() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        assertNotNull( "component", component );
        assertFalse( "initial-active-state", component.isActive() );
    }
    
   /**
    * Test that the component exposes itself as active following activation 
    * and inactive following deactivation.
    */
    public void testActivationDeactivationCycle() throws Exception
    {
        Component component = m_control.createComponent( m_model );
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
        Component component = m_control.createComponent( m_model );
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
