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
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import net.dpml.part.component.Component;
import net.dpml.part.PartContentHandlerFactory;

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
        URL url = new File( test, PATH ).toURL();
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
        Demo demo = (Demo) component.resolve( false );
        component.release( demo );
    }

    static
    {
        URLConnection.setContentHandlerFactory( new PartContentHandlerFactory() );
    }

}
