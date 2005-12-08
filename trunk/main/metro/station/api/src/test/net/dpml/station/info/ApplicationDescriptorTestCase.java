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
    
    public void testCodeBaseURISpec() throws Exception
    {
        String spec = m_codebase.toASCIIString();
        assertEquals( "codebase-spec", spec, m_descriptor.getCodeBaseURISpec() );
    }
    
    public void testCodeBaseURI() throws Exception
    {
        assertEquals( "codebase", m_codebase, m_descriptor.getCodeBaseURI() );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_descriptor );
    }

    public void testXMLEncoding() throws Exception
    {
        doEncodingTest( m_descriptor, "application.xml" );
    }
}
