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

package net.dpml.test.acme;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map.Entry;

import junit.framework.TestCase;

import net.dpml.part.control.Component;
import net.dpml.part.control.Controller;

import net.dpml.metro.central.MetroHelper;

/**
 * Test a simple component case.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class ExampleTestCase extends TestCase
{    
   /**
    * Test the construction of the example component and the invocation of
    * an operation on the example service interface.
    */
    public void testExampleUsingValue() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-example-one.part" );
        Component component = helper.getController().newComponent( uri );
        Example example = (Example) component.resolve();
        example.doMyStuff();
    }

   /**
    * Test the construction of the example component using a 
    * component declared inside the context of the target
    * component.
    */
    public void testExampleUsingComponent() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-example-two.part" );
        Component component = helper.getController().newComponent( uri );
        Example example = (Example) component.resolve();
        example.doMyStuff();
    }

   /**
    * Test the construction of the example component using a 
    * component declared as a part of an enclosing component.
    */
    public void testExampleUsingContainer() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-example-three.part" );
        Component component = helper.getController().newComponent( uri );
        Example example = (Example) component.resolve();
        example.doMyStuff();
    }

   /**
    * Test the construction of the example component using a 
    * component declared as a part of an enclosing component using 
    * a part reference.
    */
    public void testExampleUsingPartReference() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-example-four.part" );
        Component component = helper.getController().newComponent( uri );
        Example example = (Example) component.resolve();
        example.doMyStuff();
    }
}
