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

package net.dpml.http;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.ControlException;

import net.dpml.http.demo.Demo;

/**
 * Test a simple component case.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class HttpTestCase extends TestCase
{
    private static final String PATH = "demo.part";
    private static final String TEST_DIR_KEY = "project.test.dir";
    
   /**
    * Test the construction of the http demo implementation.
    * @exception Exception if an error occurs
    */
    public void testHttp() throws Exception
    {
        File test = new File( System.getProperty( TEST_DIR_KEY ) );
        URI uri = new File( test, PATH ).toURI();
        Controller control = Controller.STANDARD;
        Component component = control.createComponent( uri );
        try
        {
            Demo demo = (Demo) component.getProvider().getValue( false );
            demo.addContext( "${dpml.data}/docs", "/test" );
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
        System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/debug" );
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
