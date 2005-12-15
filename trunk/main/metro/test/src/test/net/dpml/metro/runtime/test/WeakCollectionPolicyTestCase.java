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
import net.dpml.part.Instance;

/**
 * Test WEAK collection semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class WeakCollectionPolicyTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
    public void setUp() throws Exception
    {
        final String path = "example-3.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
    public void testCollection() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
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
        // the following assertion may fail as GC behaviour is not gauranteed
        //
        
        assertEquals( "count", 0, count );
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
