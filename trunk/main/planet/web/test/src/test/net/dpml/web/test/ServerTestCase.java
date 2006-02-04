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

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.ControlException;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.security.Password;
import org.mortbay.resource.Resource;

/**
 * Test a simple component case.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class ServerTestCase extends TestCase
{
    private static final String PATH = "demo.part";
    private static final String TEST_DIR_KEY = "project.test.dir";
    
   /**
    * Test the deployment of the Jetty server.
    * @exception Exception if an error occurs
    */
    public void testServerDeployment() throws Exception
    {
        File test = new File( System.getProperty( TEST_DIR_KEY ) );
        URI uri = new File( test, PATH ).toURI();
        Controller control = Controller.STANDARD;
        Component component = control.createComponent( uri );
        try
        {
            Server server = (Server) component.getProvider().getValue( false );
            Handler[] handlers = server.getHandlers();
            assertEquals( "handler count", 2, handlers.length );
            for( int i=0; i<handlers.length; i++ )
            {
                System.out.println( handlers[i].toString() );
            }
        }
        catch( ControlException e )
        {
            Throwable cause = e.getRootCause();
            if( cause instanceof SecurityException )
            {
                final String error = 
                  "Skipping test due to security exception."
                  + cause.getMessage();
                System.out.println( error );
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            component.deactivate();
        }
    }

    static
    {
        //System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/debug" );
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
}