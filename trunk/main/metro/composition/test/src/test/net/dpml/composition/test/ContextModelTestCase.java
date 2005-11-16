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
import net.dpml.part.Controller;

import net.dpml.state.State;
import net.dpml.part.ActivationPolicy;

import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info.EntryDescriptor;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.CollectionPolicy;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;
import net.dpml.component.model.ValidationException;
import net.dpml.component.model.ValidationException.Issue;

import net.dpml.transit.model.UnknownKeyException;

import net.dpml.test.ExampleComponent;

/**
 * Test aspects of the component model implementation.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ContextModelTestCase extends TestCase
{    
    private Part m_part;
    private ComponentModel m_model;
    private ContextModel m_context;
    
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        final Controller controller = Part.CONTROLLER;
        m_part = controller.loadPart( url );
        m_model = (ComponentModel) controller.createContext( m_part );
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
    
    public void testValidationWithoutCause() throws Exception
    {
        ContextModel context = m_model.getContextModel();
        context.validate();
    }
    
    public void testValidationWithCause() throws Exception
    {
        ContextModel context = m_model.getContextModel();
        context.setEntryDirective( "color", null );
        try
        {
            context.validate();
        }
        catch( ValidationException e )
        {
            Object source = e.getSource();
            Issue[] issues = e.getIssues();
            assertEquals( "source", context, source );
            assertEquals( "issues", 1, issues.length );
        }
    }
    
    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
    }
}
