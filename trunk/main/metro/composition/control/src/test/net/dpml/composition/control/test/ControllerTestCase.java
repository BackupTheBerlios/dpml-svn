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

package net.dpml.composition.controller.test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Map.Entry;

import junit.framework.TestCase;

import net.dpml.composition.controller.CompositionController;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ComponentDirective;
import net.dpml.composition.data.DeploymentDirective;

import net.dpml.part.PartContentHandlerFactory;
import net.dpml.component.control.Controller;
import net.dpml.component.Component;
import net.dpml.component.Container;
import net.dpml.component.DuplicateKeyException;

import net.dpml.transit.model.ContentModel;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test the controller.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ControllerTestCase extends TestCase
{    
    private Controller m_controller;

    public void setUp() throws Exception
    {
        LoggingAdapter logger = new LoggingAdapter( "test" );
        m_controller = new CompositionController( logger );
    }

    public void testSomething()
    {
    }
    
    /*
    public void testSingleSimpleValue() throws Exception
    {
        String value = "abc";
        ValueDirective part = new ValueDirective( value );
        Component component = m_controller.newComponent( part );
        Object object = component.resolve();
        assertEquals( "value", value, object );
    }

    public void testMultipleSimpleValues() throws Exception
    {
        String v1 = "abc";
        ValueDirective p1 = new ValueDirective( v1 );
        Component c1 = m_controller.newComponent( "p1", p1 );

        String v2 = "xyz";
        ValueDirective p2 = new ValueDirective( v2 );
        Component c2 = m_controller.newComponent( "p2", p2 );

        Object obj1 = c1.resolve();
        Object obj2 = c2.resolve();

        assertEquals( "obj1", v1, obj1 );
        assertEquals( "obj2", v2, obj2 );

        if( obj1 == obj2 )
        {
            fail( "Objects obj1 and obj2 should not be equal." );
        }
    }

    public void testMultipleSimpleValuesWithDuplicateNames() throws Exception
    {
        String key = "key";

        ValueDirective p1 = new ValueDirective( "abc" );
        ValueDirective p2 = new ValueDirective( "xyz" );
        m_root.addComponent( "p1", p1 );

        m_root.addComponent( key, p1 );
        try
        {
            m_root.addComponent( key, p2 );
            fail( "DuplicateKeyException was not thrown" );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }
    }

    public void testValueStartup() throws Exception
    {
        m_root.addComponent( "p1", new ValueDirective( "abc" ) );
        m_root.addComponent( "p2", new ValueDirective( "xyz" ) );
        Component[] startup = m_root.getStartupSequence();
        System.out.println( "# value startup: " + startup.length );
    }

    public void testComponentAddition() throws Exception
    {
        ComponentDirective profile = new ComponentDirective( "demo", "java.lang.Object" );
        profile.setActivationPolicy( DeploymentDirective.ENABLED );
        m_root.addComponent( "demo", profile );
        Component[] startup = m_root.getStartupSequence();
        System.out.println( "# component startup: " + startup.length );
    }

    public void testMultipleComponentAddition() throws Exception
    {
        ComponentDirective p1 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p2 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p3 = new ComponentDirective( "demo", "java.lang.Object" );
        m_root.addComponent( "p1", p1 );
        m_root.addComponent( "p2", p2 );
        m_root.addComponent( "p3", p3 );
        Component[] startup = m_root.getStartupSequence();
        System.out.println( "# component startup: " + startup.length );
    }

    public void testNestedAssembly() throws Exception
    {
        ComponentDirective p1 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p2 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p3 = new ComponentDirective( "demo", "java.lang.Object" );
        m_root.addComponent( "p1", p1 );
        Container container = (Container) m_root.addComponent( "p2", p2 );
        container.addComponent( "p3", p3 );
        Component[] s1 = m_root.getStartupSequence();
        System.out.println( "# s1 startup: " + s1.length );
        for( int i=0; i<s1.length; i++ )
        {
            System.out.println( "# s1." + i + " " + s1[i] );
        }
        Component[] s2 = container.getStartupSequence();
        System.out.println( "# s2 startup: " + s2.length );
        for( int i=0; i<s2.length; i++ )
        {
            System.out.println( "# s2." + i + " " + s2[i] );
        }
    }

    public void testDependentAssembly() throws Exception
    {
        ComponentDirective p1 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p2 = new ComponentDirective( "demo", "java.lang.Object" );
        ComponentDirective p3 = new ComponentDirective( "demo", "java.lang.Object" );
    }
    */
}
