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

package net.dpml.test.runtime;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.test.composite.ChildComponent;
import net.dpml.test.composite.PartsComponent;

/**
 * Contains a series of tests applied to the composite component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartsTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "parts.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Validate composite instantiation and in particular that the color
    * assigned to the child component has been overriden by the parent. 
    * @exception Exception if an error occurs
    */
    public void testComposite() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        assertNotNull( "component", component );
        Provider instance = component.getProvider();
        PartsComponent parent = (PartsComponent) instance.getValue( false );
        Color color = parent.getColor();
        ChildComponent child = parent.getChild();
        Color c = child.getColor();
        assertEquals( "color", color, c );
    }
}
