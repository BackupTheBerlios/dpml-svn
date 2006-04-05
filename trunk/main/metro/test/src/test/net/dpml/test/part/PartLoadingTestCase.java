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

package net.dpml.test.part;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import net.dpml.part.Part;

import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.Transit;

/**
 * Testcase validating part loading from a uri.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartLoadingTestCase extends TestCase
{
    private URI m_uri;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "test.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }
    
   /**
    * Test part loading via a controller.
    * @exception Exception if an error occurs
    */
    public void testPartLoading() throws Exception
    {
        Logger.global.info( "commencing test" );
        
        // initialize transit
        
        DefaultTransitModel model = DefaultTransitModel.getDefaultModel();
        Transit.getInstance( model );
        
        // initialize the controller
        
        Part part = Part.load( m_uri );
        
        // do stuff
        
        model.dispose();
        
        Logger.global.info( "test complete." );
    }
    
    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          "net.dpml.util.ConfigurationHandler" );
        System.setProperty( 
          "dpml.logging.config",
          "file:" + System.getProperty( "project.test.dir" ) + "/logging.properties" );
    }
}
