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

import net.dpml.part.local.Controller;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.control.ComponentManager;
import net.dpml.metro.control.ContextManager;
import net.dpml.metro.model.ContextModel;
import net.dpml.metro.model.ValidationException;
import net.dpml.metro.model.ValidationException.Issue;

/**
 * Test aspects of the component context model implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextModelTestCase extends TestCase
{    
    private ComponentManager m_model;
    
   /**
    * Testcase setup during which the part definition 'example.part'
    * is established as a file uri.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "example.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = (ComponentManager) Controller.STANDARD.createModel( uri );
    }
    
   /**
    * Test context model inital state and subsequent context model updating.
    * @exception Exception if an unexpected error occurs
    */
    public void testContextModel() throws Exception
    {
        ContextManager context = m_model.getContextManager();
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
    
   /**
    * Test model vaidation (with cause for concern).
    * @exception Exception if an unexpected error occurs
    */
    public void testValidationWithoutCause() throws Exception
    {
        ContextManager context = m_model.getContextManager();
        context.validate();
    }
    
   /**
    * Test model vaidation (with cause for concern).
    * @exception Exception if an unexpected error occurs
    */
    public void testValidationWithCause() throws Exception
    {
        ContextManager context = m_model.getContextManager();
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
    
}
