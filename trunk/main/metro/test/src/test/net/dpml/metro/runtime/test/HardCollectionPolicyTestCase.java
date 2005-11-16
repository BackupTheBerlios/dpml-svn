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
import java.net.URL;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.ActivationPolicy;
import net.dpml.part.Instance;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.StateEvent;
import net.dpml.state.impl.DefaultStateListener;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ExampleComponent;

/**
 * Test HARD collection policy semantics.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class HardCollectionPolicyTestCase extends TestCase
{    
    private Part m_part;
    private ComponentModel m_model;
    private Controller m_control;
    
    public void setUp() throws Exception
    {
        final String path = "example-4.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        m_control = Part.CONTROLLER;
        Part part = m_control.loadPart( url );
        m_model = (ComponentModel) m_control.createContext( part );
    }
    
    public void testCollection() throws Exception
    {
        Component component = m_control.createComponent( m_model );
        component.activate();
        assertTrue( "is-active", component.isActive() );
        Instance one = component.getInstance();
        Instance two = component.getInstance();
        int count = component.size();
        
        //
        // this is a singleton component and we have a reference to the instance 
        // so the count should be 1
        //
        
        assertEquals( "count", 1, count );
        
        //
        // after nulling out the references and invoking a GC the count should be zero
        //
        
        one = null;
        two = null;
        System.gc();
        count = component.size();
        
        //
        // tthe count should still be 1 becuase the HARD collection policy is in place
        //
        
        assertEquals( "count", 1, count );
        component.deactivate();
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
