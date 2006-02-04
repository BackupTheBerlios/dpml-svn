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

package net.dpml.web.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;

import junit.framework.TestCase;

/**
 * Test integrity of the deployed keystore.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class KeystoreIntegrityTestCase extends TestCase
{
    private static final String TEST_DIR_KEY = "project.test.dir";
    
   /**
    * Test keystore loading (also validates that the installed keystore is not corrupted).
    */
    public void testKeystore() throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance( "JKS" );
        String password = "password";
        URL url = new URL( "local:keystore:dpml/planet/web/demo" );
        keyStore.load( url.openStream(), password.toCharArray() );
    }
    
    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
}
