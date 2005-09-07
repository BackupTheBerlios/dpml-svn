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

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Map.Entry;

import junit.framework.TestCase;

import net.dpml.part.PartContentHandlerFactory;
import net.dpml.component.control.Controller;
import net.dpml.component.Component;

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
        Component component = getComponent( PATH );
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
        Component component = getComponent( "acme-widget.part" );
        Widget widget = (Widget) component.resolve();
        widget.doWidgetStuff( "green" );
        component.release( widget );
    }

   /**
    * Test the isolation of the widget.
    */
    public void testServiceIsolation() throws Exception
    {
        Component component = getComponent( PATH );
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
        Component component = getComponent( PATH );
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
        Component component = getComponent( PATH );
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
        Component c1 = getComponent( PATH );
        Component c2 = getComponent( PATH );
        Widget w1 = (Widget) c1.resolve( false );
        Widget w2 = (Widget) c2.resolve( false );
        if( w1.equals( w2 ) )
        {
            fail( "Widget w1 and w2 are not unique!" );
        }
    }

    Component getComponent( String path ) throws Exception
    {
        File test = new File( System.getProperty( "project.test.dir" ) );
        URL url = new File( test, path ).toURL();
        return (Component) url.getContent( new Class[]{ Component.class } );
    }

    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
        URLConnection.setContentHandlerFactory( new PartContentHandlerFactory() );
    }

}
