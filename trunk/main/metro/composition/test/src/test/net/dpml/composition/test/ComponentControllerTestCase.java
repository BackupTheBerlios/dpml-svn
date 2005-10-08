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

import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentControllerTestCase extends TestCase
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
    
    public void testHandlerInitialState() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        assertNotNull( "handler", handler );
        State graph = m_model.getStateGraph();
        assertNotNull( "graph-not-null", graph );
    }
    
    public void testStateListenerAdditionAndRemoval() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Instance instance = handler.getInstance();
        StateListener listener = new DefaultStateListener();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
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
        handler.getInstance().addStateListener( listener );
        assertTrue( "is-active", handler.isActive() );
        handler.getInstance().removeStateListener( listener );
        handler.deactivate();
        assertFalse( "is-active-following-deactivation", handler.isActive() );
    }

    public void testInstanceAquisitionInInactiveState() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        try
        {
            Value value = (Value) handler.getInstance();
            fail( "Value returned in inactive state - expected IllegalStateException" );
        }
        catch( IllegalStateException e )
        {
            // success
        }
    }

    public void testValueInstantiation() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        handler.activate();
        Value value = (Value) handler.getInstance();
        Object instance = value.resolve();
        assertTrue( "isa-proxy", Proxy.isProxyClass( instance.getClass() ) );
        assertTrue( "isa-color-manager", ( instance instanceof ColorManager ) );
        assertFalse( "isa-example-component", ( instance instanceof ExampleComponent ) );
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
