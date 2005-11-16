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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.net.URI;
import java.util.Date;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.ActivationPolicy;
import net.dpml.part.Instance;
import net.dpml.part.Disposable;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.StateEvent;
import net.dpml.state.impl.DefaultStateListener;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;

import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

import net.dpml.test.ColorManager;
import net.dpml.test.ContextTestComponent;
import net.dpml.test.ContextTestComponent.Context;

/**
 * Test clean disposal.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ContextTestCase extends TestCase
{    
    private ContextTestComponent m_value;
    private Context m_context;
    
    public void setUp() throws Exception
    {
        long now = new Date().getTime();
        //System.out.println( "1" ); 
        final String path = "context.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URL url = new File( test, path ).toURL();
        Controller control = Part.CONTROLLER;
        //System.out.println( "2: " + ( new Date().getTime() - now ) ); 
        Part part = control.loadPart( url );
        //System.out.println( "3: " + ( new Date().getTime() - now ) ); 
        ComponentModel model = (ComponentModel) control.createContext( part );
        Component component = control.createComponent( model );
        //System.out.println( "4: " + ( new Date().getTime() - now ) ); 
        component.activate();
        Instance instance = component.getInstance();
        m_value = (ContextTestComponent) instance.getValue( false );
        m_context = m_value.getContext();
        //System.out.println( "5: " + ( new Date().getTime() - now ) ); 
    }
    
    public void testColor() throws Exception
    {
        assertEquals( "color/1", Color.RED, m_context.getColor() );
        assertEquals( "color/2", Color.YELLOW, m_context.getOptionalColor( Color.YELLOW ) );
    }
    
    public void testNullColor() throws Exception
    {
        assertEquals( "color-null", null, m_context.getOptionalColor( null ) );
    }
    
    public void testInteger() throws Exception
    {
        assertEquals( "int/1", 0, m_context.getInteger() );
        assertEquals( "int/2", 999, m_context.getOptionalInteger( 999 ) );
    }
    
    public void testShort() throws Exception
    {
        short s1 = 0;
        short s2 = 9;
        assertEquals( "short/1", s1, m_context.getShort() );
        assertEquals( "short/2", s2, m_context.getOptionalShort( s2 ) );
    }
    
    public void testLong() throws Exception
    {
        long v1 = 0;
        long v2 = 9;
        assertEquals( "long/1", v1, m_context.getLong() );
        assertEquals( "long/2", v2, m_context.getOptionalLong( v2 ) );
    }
    
    public void testByte() throws Exception
    {
        byte v1 = 0;
        byte v2 = 9;
        assertEquals( "byte/1", v1, m_context.getByte() );
        assertEquals( "byte/2", v2, m_context.getOptionalByte( v2 ) );
    }
    
    public void testDouble() throws Exception
    {
        double v1 = 0;
        double v2 = 9;
        double delta = 0.001;
        
        assertEquals( "double/1", v1, m_context.getDouble(), delta );
        assertEquals( "double/2", v2, m_context.getOptionalDouble( v2 ), delta );
    }
    
    public void testFloat() throws Exception
    {
        float v1 = 0.5f;
        float v2 = 3.142f;
        double delta = 0.001;
        assertEquals( "float/1", v1, m_context.getFloat(), delta );
        assertEquals( "float/2", v2, m_context.getOptionalFloat( v2 ), delta );
    }
    
    public void testChar() throws Exception
    {
        char v1 = 'x';
        char v2 = '#';
        assertEquals( "char/1", v1, m_context.getChar() );
        assertEquals( "char/2", v2, m_context.getOptionalChar( v2 ) );
    }
    
    public void testBoolean() throws Exception
    {
        assertEquals( "boolean/1", true, m_context.getBoolean() );
        assertEquals( "boolean/2", false, m_context.getOptionalBoolean( false ) );
    }
    
    public void testWorkSymbolicReference() throws Exception
    {
        final File test = new File( System.getProperty( "user.dir" ) );
        assertEquals( "file/1", test, m_context.getFile() );
        assertEquals( "file/2", test, m_context.getFile( new File( "abc" ) ) );
        final File somewhere = new File( "somewhere" );
        assertEquals( "file/3", somewhere, m_context.getOptionalFile( somewhere ) );
    }
    
    public void testURISymbolicReference() throws Exception
    {
        URI foo = new URI( "foo:bar" );
        URI uri = new URI( "component:/context" );
        assertEquals( "uri", uri, m_context.getURI() );
        assertEquals( "uri", foo, m_context.getOptionalURI( foo ) );
    }
    
    public void testNameSymbolicReference() throws Exception
    {
        assertEquals( "name", "context", m_context.getName() );
    }
    
    public void testPathSymbolicReference() throws Exception
    {
        assertEquals( "path", "/context", m_context.getPath() );
    }
    
    static
    {
        System.setProperty( 
          "java.util.prefs.PreferencesFactory", 
          "net.dpml.transit.store.LocalPreferencesFactory" );
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
