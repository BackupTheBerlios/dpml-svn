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

import net.dpml.metro.part.Part;
import net.dpml.metro.part.Controller;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.ActivationPolicy;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Directive;
import net.dpml.metro.state.State;
import net.dpml.metro.state.StateListener;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.impl.DefaultStateListener;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * Test general behaviour of an Instance without consideration for lifestyle.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class InstanceTestCase extends TestCase
{    
    private Directive m_part;
    private ComponentModel m_model;
    private Controller m_control;
    private State m_state;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_control = Part.CONTROLLER;
        m_part = m_control.loadDirective( uri );
        m_model = (ComponentModel) m_control.createContext( m_part );
    }
    
    public void testStateListenerAdditionAndRemoval() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        Instance instance = component.getInstance();
        StateListener listener = new DefaultStateListener();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.deactivate();
    }
    
    public void testNullListenerAddition() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        Instance instance = component.getInstance();
        try
        {
            instance.addStateListener( null );
            fail( "NullPointerException was not thown" );
        }
        catch( NullPointerException npe )
        {
            // success
        }
        finally
        {
            component.deactivate();
        }
    }
    
    public void testNullListenerRemoval() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        Instance instance = component.getInstance();
        try
        {
            instance.removeStateListener( null );
            fail( "NullPointerException was not thown" );
        }
        catch( NullPointerException npe )
        {
            // success
        }
        finally
        {
            component.deactivate();
        }
    }

    public void testUnknownListenerRemoval() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        Instance instance = component.getInstance();
        try
        {
            StateListener listener = new DefaultStateListener();
            instance.removeStateListener( listener );
            fail( "IllegalArgumentException was not thown" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
        finally
        {
            component.deactivate();
        }
    }
    
    public void testActivationDeactivationCycle() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        DefaultStateListener listener = new DefaultStateListener();
        listener.addPropertyChangeListener( 
          new PropertyChangeListener()
          {
            public void propertyChange( PropertyChangeEvent event )
            {
                State oldState = (State) event.getOldValue();
                State state = (State) event.getNewValue();
                // TODO: some sort of validation procedure is needed here
            }
          } );
        component.activate();
        assertTrue( "is-active", component.isActive() );
        Instance instance = component.getInstance();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.deactivate();
        assertFalse( "is-active-following-deactivation", component.isActive() );
    }

    public void testValueInstantiationWithProxy() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        try
        {
            Instance instance = component.getInstance();
            Object object = instance.getValue( true );
            assertTrue( "isa-proxy", Proxy.isProxyClass( object.getClass() ) );
            assertTrue( "isa-color-manager", ( object instanceof ColorManager ) );
            assertFalse( "isa-example-component", ( object instanceof ExampleComponent ) );
        }
        finally
        {
            component.deactivate();
        }
    }

    public void testValueInstantiationWithoutProxy() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        try
        {
            Instance instance = component.getInstance();
            Object object = instance.getValue( false );
            assertFalse( "isa-proxy", Proxy.isProxyClass( object.getClass() ) );
            assertTrue( "isa-color-manager", ( object instanceof ColorManager ) );
            assertTrue( "isa-example-component", ( object instanceof ExampleComponent ) );
        }
        finally
        {
            component.deactivate();
        }
    }
    
    public void testContextMutation() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        try
        {
            Instance instance = component.getInstance();
            ColorManager manager = (ColorManager) instance.getValue( true );
            Color color = manager.getColor();
            assertEquals( "initial-color", Color.RED, color );
            ValueDirective newDirective = new ValueDirective( Color.class.getName(), "BLUE", (String) null );
            m_model.getContextModel().setEntryDirective( "color", newDirective );
            color = manager.getColor();
            assertEquals( "mutated-color", Color.BLUE, color );
        }
        finally
        {
            component.deactivate();
        }
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
