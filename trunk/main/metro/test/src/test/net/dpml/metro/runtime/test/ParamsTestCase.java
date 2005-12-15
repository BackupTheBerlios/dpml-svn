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

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.Provider;

import net.dpml.test.params.ParameterizableComponent;

/**
 * Contains a series of tests dealing with dynamic component lifecycles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ParamsTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
    public void setUp() throws Exception
    {
        final String path = "params.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test that the component initial state is inactive.
    */
    public void testCategories() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Provider instance = component.getProvider();
        ParameterizableComponent object = (ParameterizableComponent) instance.getValue( false );
        assertEquals( "name", "fred", object.getName() );
        assertEquals( "count", ParameterizableComponent.TEST_VALUE, object.getSize() );
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
