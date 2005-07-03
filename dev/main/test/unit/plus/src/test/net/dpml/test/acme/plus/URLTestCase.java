/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.test.acme.plus;

import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import net.dpml.part.part.Part;
import net.dpml.part.manager.Component;

import net.dpml.test.acme.Dimension;

import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;

/**
 * Test URL content handling.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta
 *         Library</a>
 */
public class URLTestCase extends TestCase
{
    //--------------------------------------------------------------------------
    // tests
    //--------------------------------------------------------------------------

   /**
    * Test loading of a component instance via URL.
    */
    public void testComponentInstanceAquisition() throws Exception
    {
        URL url = new URL( SIMPLE_TEST_PART );
        Dimension d = (Dimension) url.getContent( new Class[]{ Object.class } );
    }

   /**
    * Test loading of a component provider model via URL.
    */
    public void testComponentAquisition() throws Exception
    {
        URL url = new URL( SIMPLE_TEST_PART );
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
        Dimension d = (Dimension) component.resolve();
    }

   /**
    * Test loading of a component deployment model via URL.
    */
    public void testPartAquisition() throws Exception
    {
        URL url = new URL( SIMPLE_TEST_PART );
        Part part = (Part) url.getContent();
        assertNotNull( "part", part );
    }

   /**
    * Check that the loading of a url results in the loading of the Part 
    * class from the same classloader. This validate that the caching of plugins 
    * by the Transit content manager is doing what it should be doing. If caching
    * is not performing correctly the returned part classes will not be the same.
    */
    public void testPartLoading() throws Exception
    {
        URL urlA = new URL( SIMPLE_TEST_PART );
        Part partA = (Part) urlA.getContent();
        URL urlB = new URL( PLUS_TEST_PART );
        Part partB = (Part) urlB.getContent();
        assertEquals( "same class", partA.getClass(), partB.getClass() );
    }

    //--------------------------------------------------------------------------
    // static utils
    //--------------------------------------------------------------------------

   /**
    * Setup the system properties to assign transit as a protocol handler, depot's logging 
    * configuration so things are neat, make sure prefs are enabled, and assign the composition
    * content manager plugin as the preferred content manager (which will make sure that our
    * part content request are dealt with).
    */
    private final static String SIMPLE_TEST_PART = "@SIMPLE-TEST-PART@";
    private final static String PLUS_TEST_PART = "@PLUS-TEST-PART@";
    private final static String CONTENT_MANAGER_PLUGIN = "@CONTENT-MANAGER-PLUGIN@";

    private static Transit TRANSIT;

    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.unit.LocalPreferencesFactory" );
        System.setProperty( "dpml.transit.profile", "url-testcase" );
        System.setProperty( 
         "java.util.logging.config.class", 
         System.getProperty( 
           "java.util.logging.config.class", 
           "net.dpml.transit.util.ConfigurationHandler" ) );
        try
        {
            URI uri = new URI( CONTENT_MANAGER_PLUGIN );
            TransitModel model = new DefaultTransitModel();
            model.getContentRegistryModel().setCodeBaseURI( uri );
            TRANSIT = Transit.getInstance( model );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }
}
