/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.station.info;

import java.util.Properties;
import java.net.URI;

import net.dpml.transit.info.ValueDirective;

/**
 * Test ApplicationDescriptor class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ApplicationDescriptorTestCase extends AbstractTestCase
{
    private URI m_codebase;
    private String m_title;
    private ValueDirective[] m_values;
    
    private String m_base;
    private StartupPolicy m_policy;
    private int m_startup;
    private int m_shutdown;
    private Properties m_properties;
    private URI m_config;
    
    private ApplicationDescriptor m_descriptor;

   /**
    * Test-case setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_base = "test";
        m_policy = StartupPolicy.AUTOMATIC;
        m_startup = 10;
        m_shutdown = 5;
        m_properties = new Properties();
        m_config = new URI( "local:xml:dpml/test" );
        m_title = "This is a test";
        m_codebase = new URI( "link:part:acme/widget" );
        m_values = new ValueDirective[0];
        
        m_descriptor = 
          new ApplicationDescriptor(
            m_codebase.toASCIIString(), m_title, m_values, m_base, m_policy, 
            m_startup, m_shutdown, m_properties, m_config.toASCIIString() );
    }

   /**
    * Validate that the application descriptor constructor throws 
    * an NPE is supplied with a null codebase uri.
    * @exception Exception if a error occurs
    */
    public void testNullCodeBase() throws Exception
    {
        try
        {
            new ApplicationDescriptor(
              null, m_title, m_values, m_base, m_policy, 
              m_startup, m_shutdown, m_properties, m_config.toASCIIString() );
              
            fail( "No NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test codebase uri spec accessor.
    * @exception Exception if a error occurs
    */
    public void testCodeBaseURISpec() throws Exception
    {
        String spec = m_codebase.toASCIIString();
        assertEquals( "codebase-spec", spec, m_descriptor.getCodeBaseURISpec() );
    }
    
   /**
    * Test codebase uri accessor.
    * @exception Exception if a error occurs
    */
    public void testCodeBaseURI() throws Exception
    {
        assertEquals( "codebase", m_codebase, m_descriptor.getCodeBaseURI() );
    }
    
   /**
    * Test startup timeout accessor.
    * @exception Exception if a error occurs
    */
    public void testStartupTimout() throws Exception
    {
        assertEquals( "startup-timeout", m_startup, m_descriptor.getStartupTimeout() );
    }
    
   /**
    * Test shutdown timeout accessor.
    * @exception Exception if a error occurs
    */
    public void testShutdownTimout() throws Exception
    {
        assertEquals( "shutdown-timeout", m_shutdown, m_descriptor.getShutdownTimeout() );
    }
    
   /**
    * Test title accessor.
    * @exception Exception if a error occurs
    */
    public void testTitle() throws Exception
    {
        assertEquals( "title", m_title, m_descriptor.getTitle() );
    }
    
   /**
    * Test policy accessor.
    * @exception Exception if a error occurs
    */
    public void testPolicy() throws Exception
    {
        assertEquals( "policy", m_policy, m_descriptor.getStartupPolicy() );
    }
    
   /**
    * Test value directives accessor.
    * @exception Exception if a error occurs
    */
    public void testValueDirectives() throws Exception
    {
        assertEquals( "values", m_values.length, m_descriptor.getValueDirectives().length );
    }
    
   /**
    * Test descriptor serialization.
    * @exception Exception if a error occurs
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_descriptor );
    }

}
