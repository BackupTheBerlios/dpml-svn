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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;
import net.dpml.part.Handler;
import net.dpml.part.Control;
import net.dpml.part.ActivationPolicy;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.StateEvent;
import net.dpml.state.impl.DefaultStateListener;

import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;

import net.dpml.test.ExampleComponent;

/**
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentControllerTestCase extends TestCase
{    
    private Part m_part;
    private ComponentModel m_model;
    private Control m_control;
    private State m_state;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        final PartHandler handler = Part.DEFAULT_HANDLER;
        m_part = handler.loadPart( url );
        m_model = (ComponentModel) handler.createContext( m_part );
        m_control = handler.getController( m_model );
    }
    
    public void testHandlerInitialState() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        assertNotNull( "handler", handler );
        State state = handler.getState();
        State graph = m_model.getStateGraph() ;
        assertEquals( "graph-equals-state", graph, state );
    }
    
    public void testStateListenerAdditionAndRemoval() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        StateListener listener = new DefaultStateListener();
        handler.addStateListener( listener );
        handler.removeStateListener( listener );
    }
    
    public void testNullListenerAddition() throws Exception
    {
        Handler handler = m_control.createHandler( m_model );
        try
        {
            handler.addStateListener( null );
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
        try
        {
            handler.removeStateListener( null );
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
        try
        {
            StateListener listener = new DefaultStateListener();
            handler.removeStateListener( listener );
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
                System.out.println( "state change from " + oldState + " to " + state );
                m_state = state;
            }
          } );
        handler.addStateListener( listener );
        m_control.activate( handler );
        assertTrue( "is-active", handler.isActive() );
        m_control.deactivate( handler );
        assertFalse( "is-active-following-deactivation", handler.isActive() );
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
