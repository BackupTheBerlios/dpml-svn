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

import junit.framework.TestCase;

import net.dpml.part.local.Controller;
import net.dpml.part.remote.Component;
import net.dpml.part.remote.Provider;
import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateListener;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * Test general behaviour of a Provider without consideration for lifestyle.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProviderTestCase extends TestCase
{    
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test state listener addition and removal.
    * @exception Exception if an error occurs
    */
    public void testStateListenerAdditionAndRemoval() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        Provider instance = component.getProvider();
        StateListener listener = new DefaultStateListener();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.deactivate();
    }
    
   /**
    * Test illegal null listener addition.
    * @exception Exception if an error occurs
    */
    public void testNullListenerAddition() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        Provider instance = component.getProvider();
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
    
   /**
    * Test illegal null listener removal.
    * @exception Exception if an error occurs
    */
    public void testNullListenerRemoval() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        Provider instance = component.getProvider();
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

   /**
    * Test invalid listener removal.
    * @exception Exception if an error occurs
    */
    public void testUnknownListenerRemoval() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        Provider instance = component.getProvider();
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
    
   /**
    * Test activation/deactivation cycle.
    * @exception Exception if an error occurs
    */
    public void testActivationDeactivationCycle() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        DefaultStateListener listener = new DefaultStateListener();
        listener.addPropertyChangeListener( 
          new PropertyChangeListener()
          {
            public void propertyChange( PropertyChangeEvent event )
            {
                State oldState = (State) event.getOldValue();
                State state = (State) event.getNewValue();
            }
          } );
        component.activate();
        assertTrue( "is-active", component.isActive() );
        Provider instance = component.getProvider();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.deactivate();
        assertFalse( "is-active-following-deactivation", component.isActive() );
    }

   /**
    * Test proxied value instantiation.
    * @exception Exception if an error occurs
    */
    public void testValueInstantiationWithProxy() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        try
        {
            Provider instance = component.getProvider();
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

   /**
    * Test non-proxied value instantiation.
    * @exception Exception if an error occurs
    */
    public void testValueInstantiationWithoutProxy() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        try
        {
            Provider instance = component.getProvider();
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
    
   /**
    * Test provider context mutation.
    * @exception Exception if an error occurs
    */
    public void testContextMutation() throws Exception
    {
        ComponentModel model = (ComponentModel) CONTROLLER.createModel( m_uri );
        Component component = Controller.STANDARD.createComponent( model );
        component.activate();
        try
        {
            Provider instance = component.getProvider();
            ColorManager manager = (ColorManager) instance.getValue( true );
            Color color = manager.getColor();
            assertEquals( "initial-color", Color.RED, color );
            ValueDirective newDirective = new ValueDirective( Color.class.getName(), "BLUE", (String) null );
            ContextModel context = (ContextModel) model.getContextModel();
            context.setEntryDirective( "color", newDirective );
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
