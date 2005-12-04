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
import net.dpml.metro.part.Context;
import net.dpml.metro.part.Directive;
import net.dpml.metro.part.Controller;
import net.dpml.metro.part.ActivationPolicy;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.ControlException;
import net.dpml.metro.part.Model;
import net.dpml.metro.state.State;
import net.dpml.metro.state.StateListener;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.impl.DefaultStateListener;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.composite.ChildComponent;
import net.dpml.test.composite.CompositeComponent;

/**
 * Contains a series of tests applied to the composite component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CompositeTestCase extends TestCase
{    
    private Model m_model;
    
    public void setUp() throws Exception
    {
        final String path = "composite.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = Part.CONTROLLER.createModel( uri );
    }
    
   /**
    * Validate composite instantiation and in particular that the color
    * assigned to the child component has been overriden by the parent. 
    */
    public void testComposite() throws Exception
    {
        Component component = Part.CONTROLLER.createComponent( m_model );
        assertNotNull( "component", component );
        Instance instance = component.getInstance();
        CompositeComponent parent = (CompositeComponent) instance.getValue( false );
        Color color = parent.getColor();
        ChildComponent child = parent.getChild();
        Color c = child.getColor();
        assertEquals( "color", color, c );
    }
    
   /**
    * Validate composite instantiation with an overloader parent context.
    */
    public void testOverloadedComposite() throws Exception
    {
        Component component = Part.CONTROLLER.createComponent( m_model );
        ((Context)component).getContextMap().put( "color", Color.YELLOW );
        Instance instance = component.getInstance();
        CompositeComponent parent = (CompositeComponent) instance.getValue( false );
        Color color = parent.getColor();
        assertEquals( "parent-color", Color.YELLOW, color );
        ChildComponent child = parent.getChild();
        Color c = child.getColor();
        assertEquals( "color", color, c );
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
