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

import net.dpml.metro.part.Part;
import net.dpml.metro.part.Directive;
import net.dpml.metro.part.Controller;
import net.dpml.metro.part.Model;
import net.dpml.metro.part.Component;

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
    * Test the construction of the widget implementation and invocation
    * of a non-service public method on the implementation class.
    */
    public void testHttp() throws Exception
    {
        File test = new File( System.getProperty( TEST_DIR_KEY ) );
        URI uri = new File( test, PATH ).toURI();
        
        Controller control = Part.CONTROLLER;
        Directive directive = control.loadDirective( uri );
        Model context = control.createContext( directive );
        Component component = control.createComponent( context );
        component.activate();
        Demo demo = (Demo) component.getInstance().getValue( false );
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
