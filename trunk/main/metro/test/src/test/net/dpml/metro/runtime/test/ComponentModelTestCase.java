/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.runtime.test;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.metro.part.Part;
import net.dpml.metro.part.Directive;
import net.dpml.metro.part.Controller;

import net.dpml.metro.state.State;
import net.dpml.metro.part.ActivationPolicy;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.part.Directive;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;

import net.dpml.test.ExampleComponent;

/**
 * Test aspects of the component model implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentModelTestCase extends TestCase
{    
    private ComponentModel m_model;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = (ComponentModel) Part.CONTROLLER.createModel( uri );
    }
    
    public void testName() throws Exception
    {
        String name = "example"; // from build.xml's component directive 
        assertEquals( "name", name, m_model.getName() );
    }
    
    public void testContextPath() throws Exception
    {
        String path = "/example";
        assertEquals( "path", path, m_model.getContextPath() );
    }
    
    public void testImplementationClassName() throws Exception
    {
        String classname = ExampleComponent.class.getName();
        assertEquals( "classname", classname, m_model.getImplementationClassName() );
    }
    
    public void testStateGraph() throws Exception
    {
        State state = m_model.getStateGraph();
        assertEquals( "substates", 2, state.getStates().length );
    }
    
    public void testActivationPolicy() throws Exception
    {
        assertEquals( "initial-activation", ActivationPolicy.STARTUP, m_model.getActivationPolicy() );
        ActivationPolicy policy = ActivationPolicy.DEMAND;
        m_model.setActivationPolicy( policy );
        assertEquals( "mutated-activation", policy, m_model.getActivationPolicy() );
    }
    
    public void testLifestylePolicy() throws Exception
    {
        assertEquals( "initial-lifestyle", LifestylePolicy.TRANSIENT, m_model.getLifestylePolicy() );
    }
    
    public void testCollectionPolicy() throws Exception
    {
        assertEquals( "initial-collection", CollectionPolicy.SYSTEM, m_model.getCollectionPolicy() );
        CollectionPolicy policy = CollectionPolicy.SOFT;
        m_model.setCollectionPolicy( policy );
        assertEquals( "mutated-collection", policy, m_model.getCollectionPolicy() );
    }
    
    public void testPartKeys() throws Exception
    {
        String[] keys = m_model.getPartKeys();
        assertEquals( "parts-length", 0, keys.length );
    }
    
    public void testUnknownPart() throws Exception
    {
        try
        {
            ComponentModel child = m_model.getComponentModel( "xyz");
        }
        catch( UnknownKeyException e )
        {
            // correct
        }
    }
    
}
