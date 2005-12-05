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

import net.dpml.metro.part.Part;
import net.dpml.metro.part.Instance;
import net.dpml.metro.part.Component;
import net.dpml.metro.part.Model;

import net.dpml.test.params.ParameterizableComponent;

/**
 * Contains a series of tests dealing with dynamic component lifecycles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ParamsTestCase extends TestCase
{    
    private Model m_model;
    
    public void setUp() throws Exception
    {
        final String path = "params.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = Part.CONTROLLER.createModel( uri );
    }
    
   /**
    * Test that the component initial state is inactive.
    */
    public void testCategories() throws Exception
    {
        Component component = Part.CONTROLLER.createComponent( m_model );
        Instance instance = component.getInstance();
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
