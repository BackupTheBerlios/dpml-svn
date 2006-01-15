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

import net.dpml.part.local.Controller;
import net.dpml.part.remote.Component;
import net.dpml.part.remote.Provider;

import net.dpml.test.categories.CategoriesComponent;

/**
 * Contains a series of tests dealing with dynamic component lifecycles.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoriesTestCase extends TestCase
{   
    private static final Controller CONTROLLER = Controller.STANDARD;
    
    private URI m_uri;
    
   /**
    * Testcase setup during which the part defintion 'categories.part'
    * is established as a file uri.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "categories.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Load the categories component.
    * @exception Exception if an unexpected error occurs
    */
    public void testCategories() throws Exception
    {
        Component component = CONTROLLER.createComponent( m_uri );
        Provider provider = component.getProvider();
        CategoriesComponent instance = (CategoriesComponent) provider.getValue( false );
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
