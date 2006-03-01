/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.transit;

import java.net.URI;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.lang.Plugin;
import net.dpml.lang.Strategy;
import net.dpml.lang.Category;
import net.dpml.lang.Classpath;

import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Plugin test case.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginTestCase extends TestCase
{
    private URI[] m_system;
    private URI[] m_public;
    private URI[] m_protected;
    private URI[] m_private;
    private Classpath m_classpath;
    private String m_title = "Sample Plugin";
    private String m_description = "Plugin generated via a testcase used for validation of the externalization of a plugin definition to XML and the subsequent reading in of the XML form and cronstruction of an equivalent plugin definition.";
    
   /**
    * Testcase setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        m_system = 
          new URI[]
          {
            new URI( "thing:a" ), 
            new URI( "thing:b" )
          };
        m_public = 
          new URI[]
          {
            new URI( "thing:c" ), 
            new URI( "thing:d" ),
            new URI( "thing:e" )
          };
        m_protected = 
          new URI[]
          {
            new URI( "thing:f" )
          };
        m_private = 
          new URI[]
          {
            new URI( "thing:g" ),
            new URI( "thing:h" ),
            new URI( "thing:i" ),
            new URI( "thing:j" )
          };
        m_classpath = new Classpath( m_system, m_public, m_protected, m_private );
    }

   /**
    * Test invalid null path argument.
    * @exception Exception if an unexpected error occurs
    */
    public void testPluginCreation() throws Exception
    {
        URI uri = new URI( "plugin:test" );
        Properties properties = new Properties();
        properties.setProperty( "foo", "bar" );
        properties.setProperty( "pi", "3.142" );
        Strategy strategy = new DefaultStrategy( "Test", properties );
        Plugin plugin = 
          new DefaultPlugin( 
            m_title, m_description, uri, strategy, m_classpath );
        assertEquals( "title", m_title, plugin.getTitle() );
        assertEquals( "description", m_description, plugin.getDescription() );
        assertEquals( "uri", uri, plugin.getURI() );
        assertEquals( "strategy", strategy, plugin.getStrategy() );
        Classpath classpath = plugin.getClasspath();
        assertEquals( "classpath", m_classpath, classpath );
        assertEquals( "system", m_system.length, classpath.getDependencies( Category.SYSTEM ).length );
        assertEquals( "public", m_public.length, classpath.getDependencies( Category.PUBLIC ).length );
        assertEquals( "protected", m_protected.length, classpath.getDependencies( Category.PROTECTED ).length );
        assertEquals( "private", m_private.length, classpath.getDependencies( Category.PRIVATE ).length );
    }
    
   /**
    * Test the generation of a plugin xml file using the simple strategy.
    * @exception Exception if an unexpected error occurs
    */
    public void testSimpleStrategyExternalization() throws Exception
    {
        File file = new File( "target/test/plugin.xml" );
        Properties properties = new Properties();
        properties.setProperty( "foo", "bar" );
        properties.setProperty( "pi", "3.142" );
        URI uri = file.toURI();
        Strategy strategy = new DefaultStrategy( "Test", properties );
        Plugin plugin = 
          new DefaultPlugin( 
            m_title, m_description, uri, strategy, m_classpath );
        FileOutputStream output = new FileOutputStream( file );
        plugin.write( output );
        PluginBuilder loader = new PluginBuilder( new LoggingAdapter() );
        Plugin p = loader.load( file.toURI().toURL() );
        assertEquals( "plugin", plugin, p );
    }

}
