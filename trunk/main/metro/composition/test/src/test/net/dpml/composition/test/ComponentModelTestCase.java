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

package net.dpml.composition.test;

import java.awt.Color;
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;

import net.dpml.state.State;
import net.dpml.part.ActivationPolicy;

import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info.EntryDescriptor;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.CollectionPolicy;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;

import net.dpml.test.ExampleComponent;

/**
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentModelTestCase extends TestCase
{    
    private Part m_part;
    private ComponentModel m_model;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        final PartHandler handler = Part.DEFAULT_HANDLER;
        m_part = handler.loadPart( url );
        m_model = (ComponentModel) handler.createContext( m_part );
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
        LifestylePolicy policy = LifestylePolicy.SINGLETON;
        m_model.setLifestylePolicy( policy );
        assertEquals( "mutated-lifestyle", policy, m_model.getLifestylePolicy() );
    }
    
    public void testCollectionPolicy() throws Exception
    {
        assertEquals( "initial-collection", CollectionPolicy.SYSTEM, m_model.getCollectionPolicy() );
        CollectionPolicy policy = CollectionPolicy.SOFT;
        m_model.setCollectionPolicy( policy );
        assertEquals( "mutated-collection", policy, m_model.getCollectionPolicy() );
    }
    
    public void testContextModel() throws Exception
    {
        ContextModel context = m_model.getContextModel();
        assertNotNull( "context", context );
        EntryDescriptor[] entries = context.getEntryDescriptors();
        assertEquals( "entries", 1, entries.length );
        EntryDescriptor entry = entries[0];
        String key = entry.getKey();
        ValueDirective directive = (ValueDirective) context.getEntryDirective( key );
        Color color = (Color) directive.resolve();
        assertEquals( "color", Color.RED, color );
        ValueDirective newDirective = new ValueDirective( Color.class.getName(), "BLUE", (String) null );
        context.setEntryDirective( key, newDirective );
        ValueDirective result = (ValueDirective) context.getEntryDirective( key );
        assertEquals( "directive", newDirective, result );
        assertEquals( "color-2", Color.BLUE, result.resolve() );
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
    
    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
    }
}
