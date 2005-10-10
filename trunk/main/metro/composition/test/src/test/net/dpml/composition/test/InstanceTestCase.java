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

package net.dpml.composition.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;
import net.dpml.part.Handler;
import net.dpml.part.ActivationPolicy;
import net.dpml.part.Instance;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.StateEvent;
import net.dpml.state.impl.DefaultStateListener;

import net.dpml.component.data.ValueDirective;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * Test general behaviour of an Instance without consideration for lifestyle.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class InstanceTestCase extends TestCase
{    
    private Part m_part;
    private ComponentModel m_model;
    private PartHandler m_control;
    private State m_state;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        m_control = Part.DEFAULT_HANDLER;
        m_part = m_control.loadPart( url );
        m_model = (ComponentModel) m_control.createContext( m_part );
    }
    
    public void testStateListenerAdditionAndRemoval() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Instance instance = handler.getInstance();
        StateListener listener = new DefaultStateListener();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        handler.deactivate();
    }
    
    public void testNullListenerAddition() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Instance instance = handler.getInstance();
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
            handler.deactivate();
        }
    }
    
    public void testNullListenerRemoval() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Instance instance = handler.getInstance();
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
            handler.deactivate();
        }
    }

    public void testUnknownListenerRemoval() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Instance instance = handler.getInstance();
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
            handler.deactivate();
        }
    }
    
    public void testActivationDeactivationCycle() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
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
        handler.activate();
        assertTrue( "is-active", handler.isActive() );
        Instance instance = handler.getInstance();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        handler.deactivate();
        assertFalse( "is-active-following-deactivation", handler.isActive() );
    }

    public void testValueInstantiationWithProxy() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        try
        {
            Instance instance = handler.getInstance();
            Object object = instance.getValue( true );
            assertTrue( "isa-proxy", Proxy.isProxyClass( object.getClass() ) );
            assertTrue( "isa-color-manager", ( object instanceof ColorManager ) );
            assertFalse( "isa-example-component", ( object instanceof ExampleComponent ) );
        }
        finally
        {
            handler.deactivate();
        }
    }

    public void testValueInstantiationWithoutProxy() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        try
        {
            Instance instance = handler.getInstance();
            Object object = instance.getValue( false );
            assertFalse( "isa-proxy", Proxy.isProxyClass( object.getClass() ) );
            assertTrue( "isa-color-manager", ( object instanceof ColorManager ) );
            assertTrue( "isa-example-component", ( object instanceof ExampleComponent ) );
        }
        finally
        {
            handler.deactivate();
        }
    }
    
    public void testContextMutation() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        try
        {
            Instance instance = handler.getInstance();
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
            handler.deactivate();
        }
    }

    static
    {
        System.setProperty( 
          "java.util.prefs.PreferencesFactory", 
          "net.dpml.transit.store.LocalPreferencesFactory" );
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
