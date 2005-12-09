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

import java.util.Arrays;
import java.util.Properties;
import java.net.URI;

import net.dpml.transit.info.ValueDirective;
import net.dpml.station.info.RegistryDescriptor.Entry;

/**
 * Test ApplicationDescriptor class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class RegistryDescriptorTestCase extends AbstractTestCase
{
    Entry[] m_entries = new Entry[3];
    
    private RegistryDescriptor m_descriptor;

    public void setUp() throws Exception
    {
        m_entries[0] = 
          new Entry( 
            "aaa", 
            new ApplicationDescriptor(
              "link:part:acme/demo", "test", new ValueDirective[0], null, StartupPolicy.AUTOMATIC, 
              0, 0, new Properties(), null ) );
        m_entries[1] = 
          new Entry( 
            "bbb", 
            new ApplicationDescriptor(
              "link:part:acme/demo", "test", new ValueDirective[0], null, StartupPolicy.AUTOMATIC, 
              0, 0, new Properties(), null ) );
        m_entries[2] = 
          new Entry( 
            "ccc", 
            new ApplicationDescriptor(
              "link:part:acme/demo", "test", new ValueDirective[0], null, StartupPolicy.AUTOMATIC, 
              0, 0, new Properties(), null ) );

        m_descriptor = new RegistryDescriptor( m_entries );
    }
    
    public void testNullEntries()
    {
        try
        {
            new RegistryDescriptor( null );
            fail( "No NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testNullEntry()
    {
        try
        {
            new RegistryDescriptor( new Entry[]{ null } );
            fail( "No NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testEntries() throws Exception
    {
        Entry[] entries = m_descriptor.getEntries();
        if( !Arrays.equals( m_entries, entries ) )
        {
            fail( "entry-erray nbot equal" );
        }
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_descriptor );
    }

    public void testXMLEncoding() throws Exception
    {
        doEncodingTest( m_descriptor, "registry.xml" );
    }
}
