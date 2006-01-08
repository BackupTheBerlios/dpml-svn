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

/**
 * Test HARD collection policy semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HardCollectionPolicyTestCase extends TestCase
{    
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Testcase setup.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example-4.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test the HARD collection policy through the creation of two components
    * followed by a gc run and validating of the number of references remaining in 
    * memory (which according to the HARD collection policy will remain as 2).
    *
    * @exception Exception if an unexpected error occurs
    */
    public void testCollection() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.activate();
        assertTrue( "is-active", component.isActive() );
        Provider one = component.getProvider();
        Provider two = component.getProvider();
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
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
