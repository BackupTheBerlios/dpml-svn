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

package net.dpml.composition.control.test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Map.Entry;

import junit.framework.TestCase;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ComponentProfile;
import net.dpml.composition.data.DeploymentProfile;

import net.dpml.part.PartContentHandlerFactory;
import net.dpml.part.control.Controller;
import net.dpml.part.component.Component;
import net.dpml.part.component.Container;
import net.dpml.part.component.DuplicateKeyException;

import net.dpml.transit.model.ContentModel;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test the controller.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ControllerTestCase extends TestCase
{    
    private Container m_root;

    public void setUp() throws Exception
    {
        PartContentHandlerFactory factory = new PartContentHandlerFactory();
        ContentModel model = factory.newContentModel();
        LoggingAdapter logger = new LoggingAdapter( "test" );
        CompositionController controller = new CompositionController( logger, model );
        m_root = controller.getContainer();
    }

    public void testSingleSimpleValue() throws Exception
    {
        String value = "abc";
        ValueDirective part = new ValueDirective( value );
        Component component = m_root.addComponent( "testing", part );
        Object object = component.resolve();
        assertEquals( "value", value, object );
    }

    public void testMultipleSimpleValues() throws Exception
    {
        String v1 = "abc";
        ValueDirective p1 = new ValueDirective( v1 );
        Component c1 = m_root.addComponent( "p1", p1 );

        String v2 = "xyz";
        ValueDirective p2 = new ValueDirective( v2 );
        Component c2 = m_root.addComponent( "p2", p2 );

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
        ComponentProfile profile = new ComponentProfile( "demo", "java.lang.Object" );
        profile.setActivationPolicy( DeploymentProfile.ENABLED );
        m_root.addComponent( "demo", profile );
        Component[] startup = m_root.getStartupSequence();
        System.out.println( "# component startup: " + startup.length );
    }

    public void testMultipleComponentAddition() throws Exception
    {
        ComponentProfile p1 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p2 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p3 = new ComponentProfile( "demo", "java.lang.Object" );
        m_root.addComponent( "p1", p1 );
        m_root.addComponent( "p2", p2 );
        m_root.addComponent( "p3", p3 );
        Component[] startup = m_root.getStartupSequence();
        System.out.println( "# component startup: " + startup.length );
    }

    public void testNestedAssembly() throws Exception
    {
        ComponentProfile p1 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p2 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p3 = new ComponentProfile( "demo", "java.lang.Object" );
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
        /*
        ComponentProfile p1 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p2 = new ComponentProfile( "demo", "java.lang.Object" );
        ComponentProfile p3 = new ComponentProfile( "demo", "java.lang.Object" );
        */
    }
}
