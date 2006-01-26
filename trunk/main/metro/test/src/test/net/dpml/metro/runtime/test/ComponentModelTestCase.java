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
import net.dpml.part.ActivationPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.ComponentModel;

import net.dpml.test.ExampleComponent;

/**
 * Test aspects of the component model implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentModelTestCase extends TestCase
{    
    private static final Controller CONTROLLER = Controller.STANDARD;

    private ComponentModel m_model;
    
   /**
    * Testcase setup during which the part defintion 'example.part'
    * is established as a file uri.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = (ComponentModel) CONTROLLER.createModel( uri );
    }
    
   /**
    * Test the component name.
    * @exception Exception if an unexpected error occurs
    */
    public void testName() throws Exception
    {
        String name = "example"; // from build.xml's component directive 
        assertEquals( "name", name, m_model.getName() );
    }
    
   /**
    * Test the component path.
    * @exception Exception if an unexpected error occurs
    */
    public void testContextPath() throws Exception
    {
        String path = "/example";
        assertEquals( "path", path, m_model.getContextPath() );
    }
    
   /**
    * Test the component implementation classname.
    * @exception Exception if an unexpected error occurs
    */
    public void testImplementationClassName() throws Exception
    {
        String classname = ExampleComponent.class.getName();
        assertEquals( "classname", classname, m_model.getImplementationClassName() );
    }
    
   /**
    * Test the component activation policy.
    * @exception Exception if an unexpected error occurs
    */
    public void testActivationPolicy() throws Exception
    {
        assertEquals( "initial-activation", ActivationPolicy.STARTUP, m_model.getActivationPolicy() );
        ActivationPolicy policy = ActivationPolicy.DEMAND;
        
        // uncomment remainder as this is under the ComponentManager
        //m_model.setActivationPolicy( policy );
        //assertEquals( "mutated-activation", policy, m_model.getActivationPolicy() );
    }
    
   /**
    * Test the component lifestyle policy.
    * @exception Exception if an unexpected error occurs
    */
    public void testLifestylePolicy() throws Exception
    {
        assertEquals( "initial-lifestyle", LifestylePolicy.THREAD, m_model.getLifestylePolicy() );
    }
    
   /**
    * Test the component collection policy.
    * @exception Exception if an unexpected error occurs
    */
    public void testCollectionPolicy() throws Exception
    {
        assertEquals( "initial-collection", CollectionPolicy.SYSTEM, m_model.getCollectionPolicy() );
        CollectionPolicy policy = CollectionPolicy.SOFT;

        // uncomment remainder as this is under the ComponentManager
        //m_model.setCollectionPolicy( policy );
        //assertEquals( "mutated-collection", policy, m_model.getCollectionPolicy() );
    }
    
   /**
    * Test the component part keys.
    * @exception Exception if an unexpected error occurs
    */
    //public void testPartKeys() throws Exception
    //{
    //    String[] keys = m_model.getPartKeys();
    //    assertEquals( "parts-length", 0, keys.length );
    //}
    
   /**
    * Test an unknown part request failure.
    * @exception Exception if an unexpected error occurs
    */
    //public void testUnknownPart() throws Exception
    //{
    //    try
    //    {
    //        ComponentModel child = m_model.getComponentModel( "xyz" );
    //    }
    //    catch( UnknownKeyException e )
    //    {
    //        // correct
    //    }
    //}
    
}
