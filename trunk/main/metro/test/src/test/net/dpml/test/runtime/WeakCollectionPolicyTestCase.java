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

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

/**
 * Test WEAK collection semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class WeakCollectionPolicyTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example-3.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test weak collection semantics.
    * @exception Exception if an error occurs
    */
    public void testCollection() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        component.commission();
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
        // the following assertion may fail as GC behaviour is not gauranteed
        //
        
        assertEquals( "count", 0, count );
        component.decommission();
    }
}
