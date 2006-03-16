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

import java.awt.Color;
import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.metro.ComponentHandler;

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
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Testcase setup during which the part definition 'composite.part'
    * is established as a file uri.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "composite.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Validate composite instantiation and in particular that the color
    * assigned to the child component has been overriden by the parent. 
    * @exception Exception if an unexpected error occurs
    */
    public void testComposite() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        assertNotNull( "component", component );
        Provider instance = component.getProvider();
        CompositeComponent parent = (CompositeComponent) instance.getValue( false );
        Color color = parent.getColor();
        ChildComponent child = parent.getChild();
        Color c = child.getColor();
        assertEquals( "color", color, c );
    }
    
   /**
    * Validate composite instantiation with an overloader parent context.
    * @exception Exception if an unexpected error occurs
    */
    public void testOverloadedComposite() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        ComponentHandler handler = (ComponentHandler) component;
        handler.getContextMap().put( "color", Color.YELLOW );
        Provider instance = component.getProvider();
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
