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

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.metro.part.Controller;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.Instance;

import net.dpml.test.config.ConfigurableComponent;

/**
 * Contains a series of tests dealing with dynamic component lifecycles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConfigTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
    public void setUp() throws Exception
    {
        final String path = "config.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test that the component initial state is inactive.
    */
    public void testCategories() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Instance instance = component.getInstance();
        ConfigurableComponent object = (ConfigurableComponent) instance.getValue( false );
        assertEquals( "name", "fred", object.getName() );
        assertEquals( "count", 3, object.getItemCount() );
        component.deactivate();
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
