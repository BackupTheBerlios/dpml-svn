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

import net.dpml.part.control.Controller;
import net.dpml.part.component.Component;

import net.dpml.metro.central.MetroHelper;

/**
 * Test a simple component case.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class SimpleTestCase extends TestCase
{
    private static final String PATH = "acme-simple.part";

    public void tearDown()
    {
        System.gc();
    }
    
   /**
    * Test the construction of the widget component and the invocation of
    * an operation on the widget service interface.
    */
    public void testGetValue() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( PATH );
        Component component = helper.getController().newComponent( uri );
        Widget widget = (Widget) component.resolve();
        widget.doWidgetStuff( "green" );
        component.release( widget );
    }

   /**
    * Test the construction of the widget component using the 
    * DimensionComponent as the Dimension provider.
    */
    public void testDimensionInWidget() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-widget.part" );
        Component component = helper.getController().newComponent( uri );
        Widget widget = (Widget) component.resolve();
        widget.doWidgetStuff( "green" );
        component.release( widget );
    }

   /**
    * Test the isolation of the widget.
    */
    public void testServiceIsolation() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( PATH );
        Component component = helper.getController().newComponent( uri );
        Widget widget = (Widget) component.resolve();
        try
        {
            WidgetComponent w = (WidgetComponent) widget;
            fail( "Casting to implementation class!" );
        }
        catch( ClassCastException e )
        {
            // success
        }
        finally
        {
            component.release( widget );
        }
    }

   /**
    * Test the construction of the widget implementation and invocation
    * of a non-service public method on the implementation class.
    */
    public void testNonProxiedCreation() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( PATH );
        Component component = helper.getController().newComponent( uri );
        WidgetComponent widget = (WidgetComponent) component.resolve( false );
        String name = widget.getName();
        assertEquals( "name", "widget", name );
        String newName = "freight";
        widget.setName( newName );
        assertEquals( "name", newName, widget.getName() );
        component.release( widget );
    }

   /**
    * Test the construction of the widget implementation and invocation
    * of a non-service public method on the implementation class.
    */
    public void testIdentifiableManagerCreation() throws Exception
    {
        String id = "steve";
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( PATH );
        Component component = helper.getController().newComponent( uri );
        WidgetComponent widget = (WidgetComponent) component.resolve( false );
        component.release( widget );
    }

   /**
    * Test the construction of an identifiable widget instance. We create 
    * widgets w1 and w2 under unique ids and check that they are in fact 
    * unique.  Then we create w3 using the same key as we used for w1 and
    * check that the instances are the same (which is dependent on the 
    * implementation providing a rational equals implementation).
    */
    public void testIdentifiableCreation() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( PATH );
        Component c1 = helper.getController().newComponent( uri );
        Component c2 = helper.getController().newComponent( uri );
        Widget w1 = (Widget) c1.resolve( false );
        Widget w2 = (Widget) c2.resolve( false );
        if( w1.equals( w2 ) )
        {
            fail( "Widget w1 and w2 are not unique!" );
        }
    }
}
