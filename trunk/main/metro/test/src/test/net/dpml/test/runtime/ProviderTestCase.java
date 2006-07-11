/*
 * Copyright 2005-2006 Stephen J. McConnell.
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
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.state.StateListener;
import net.dpml.state.StateEvent;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

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
        component.commission();
        Provider instance = component.getProvider();
        StateListener listener = new DefaultStateListener();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.decommission();
    }
    
   /**
    * Test illegal null listener addition.
    * @exception Exception if an error occurs
    */
    public void testNullListenerAddition() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
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
            component.decommission();
        }
    }
    
   /**
    * Test illegal null listener removal.
    * @exception Exception if an error occurs
    */
    public void testNullListenerRemoval() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
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
            component.decommission();
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
        component.commission();
        assertTrue( "is-active", component.isActive() );
        Provider instance = component.getProvider();
        instance.addStateListener( listener );
        instance.removeStateListener( listener );
        component.decommission();
        assertFalse( "is-active-following-deactivation", component.isActive() );
    }

   /**
    * Test proxied value instantiation.
    * @exception Exception if an error occurs
    */
    public void testValueInstantiationWithProxy() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
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
            component.decommission();
        }
    }

   /**
    * Test non-proxied value instantiation.
    * @exception Exception if an error occurs
    */
    public void testValueInstantiationWithoutProxy() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
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
            component.decommission();
        }
    }
    
   /**
    * State event listener.
    */
    private class DefaultStateListener implements StateListener
    {
        private Logger m_logger = new DefaultLogger( "test" );
       
       /**
        * Handle a state change notification.
        * @param event the state change event
        */
        public void stateChanged( StateEvent event )
        {
            m_logger.info( "event: " + event );
        }
    }
}
