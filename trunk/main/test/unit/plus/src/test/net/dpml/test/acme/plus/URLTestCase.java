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
import java.util.logging.Logger;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.component.runtime.Component;

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
        //Logger.getLogger( "testcase" ).info( "starting aquisition" );
        URL url = new URL( SIMPLE_TEST_PART );
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
        Dimension d = (Dimension) component.resolve();
        //Logger.getLogger( "testcase" ).info( "aquisition complete" );
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
    private final static String PART_MANAGER_PLUGIN = "@PART-MANAGER-PLUGIN@";

    private static Transit TRANSIT;

    static
    {
        //
        // make sure transit is declared as the protocol handler
        //

        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );

        //
        // use an in-memory preference store so we don't cause any problems with the
        // users operational preferences
        //

        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
        //System.setProperty( "dpml.transit.profile", "url-testcase" );

        //
        // setup the classic console based logging output
        //

        System.setProperty( 
         "java.util.logging.config.class", 
         System.getProperty( 
           "java.util.logging.config.class", 
           "net.dpml.transit.util.ConfigurationHandler" ) );

        //
        // create a new transit model and setup the content handler model
        // to use within the test
        //

        try
        {
            URI uri = new URI( PART_MANAGER_PLUGIN );
            TransitModel model = new DefaultTransitModel();
            model.getContentRegistryModel().addContentModel( "part" );
            model.getContentRegistryModel().getContentModel( "part" ).setCodeBaseURI( uri );
            TRANSIT = Transit.getInstance( model );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }
}
