/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.info.test;

import net.dpml.metro.info.Descriptor;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.ThreadSafePolicy;

import net.dpml.lang.Version;

/**
 * InfoDescriptorTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InfoDescriptorTestCase extends AbstractDescriptorTestCase
{
    private final String m_name = "name";
    private final String m_classname = InfoDescriptorTestCase.class.getName();
    private final Version m_version = Version.parse( "1.2.3" );
    private final LifestylePolicy m_lifestyle = LifestylePolicy.SINGLETON;
    private final CollectionPolicy m_collection = CollectionPolicy.WEAK;
    private final ThreadSafePolicy m_threadsafe = ThreadSafePolicy.FALSE;
    
    private InfoDescriptor m_info;
    
   /**
    * Setup the testcase.
    * @exception Exception if an error occurs in setup
    */
    public void setUp() throws Exception
    {
        m_info = getInfoDescriptor();
    }
    
   /**
    * Test the name.
    */
    public void testName()
    {
        assertEquals( m_name, m_info.getName() );
    }
    
   /**
    * Test the classname.
    */
    public void testClassName()
    {
        assertEquals( m_classname, m_info.getClassname() );
    }
    
   /**
    * Test the version.
    */
    public void testVersion()
    {
        assertEquals( m_version, m_info.getVersion() );
    }
    
   /**
    * Test the lifestyle policy.
    */
    public void testLifestyle()
    {
        assertEquals( m_lifestyle, m_info.getLifestylePolicy() );
    }
    
   /**
    * Test the threadsafe default lifestyle policy.
    */
    public void testThreadSafeLifestyle()
    {
        InfoDescriptor info = new InfoDescriptor(
          m_name, m_classname, m_version, null, m_collection,
          ThreadSafePolicy.TRUE, getProperties() );
        LifestylePolicy lifestyle = info.getLifestylePolicy();
        assertEquals( "default-threadsafe-lifestyle", LifestylePolicy.SINGLETON, lifestyle );
    }
    
   /**
    * Test the non-threadsafe default lifestyle policy.
    */
    public void testNonThreadSafeLifestyle()
    {
        InfoDescriptor info = new InfoDescriptor(
          m_name, m_classname, m_version, null, m_collection,
          ThreadSafePolicy.FALSE, getProperties() );
        LifestylePolicy lifestyle = info.getLifestylePolicy();
        assertEquals( "default-non-threadsafe-lifestyle", LifestylePolicy.THREAD, lifestyle );
    }
    
   /**
    * Test the thread safe policy.
    */
    public void testThreadsafeCapable()
    {
        assertEquals( m_threadsafe, m_info.getThreadSafePolicy() );
    }
    
   /**
    * Test the collection policy.
    */
    public void testCollectionPolicy()
    {
        assertEquals( m_collection, m_info.getCollectionPolicy() );
    }
    
   /**
    * Return the info descriptor to test.
    * @return the info descriptor
    */
    protected Descriptor getDescriptor()
    {
        return getInfoDescriptor();
    }
    
   /**
    * Return the info descriptor to test.
    * @return the info descriptor
    */
    protected InfoDescriptor getInfoDescriptor()
    {
        return new InfoDescriptor(
          m_name, m_classname, m_version, m_lifestyle, m_collection, 
          m_threadsafe, getProperties() );
    }

   /**
    * Validate an info descriptor.
    * @param desc the info descriptor to validate
    */
    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        InfoDescriptor info = (InfoDescriptor) desc;
        assertEquals( m_name, info.getName() );
        assertEquals( m_classname, info.getClassname() );
        assertEquals( m_version, info.getVersion() );
        assertEquals( m_lifestyle, info.getLifestylePolicy() );
        assertEquals( m_threadsafe, info.getThreadSafePolicy() );
        assertEquals( m_collection, info.getCollectionPolicy() );
    }

   /**
    * Test null classname in constructor.
    */
    public void testNullClassnameConstructor()
    {
        try
        {
            new InfoDescriptor(
              m_name, null, m_version, m_lifestyle, m_collection, 
              m_threadsafe, getProperties() );
            fail( "Did not throw a NullPointerException" );
        }
        catch( NullPointerException npe )
        {
            // Success!
        }
    }
    
   /**
    * Test bad classname in constructor.
    */
    public void testBadClassnameConstructor()
    {
        try
        {
            new InfoDescriptor(
              m_name, "foo/fake/ClassName", m_version, m_lifestyle, m_collection,
              m_threadsafe, getProperties() );
            fail( "Did not throw the proper IllegalArgumentException" );
        }
        catch( IllegalArgumentException iae )
        {
            // Success!
        }
    }
    
   /**
    * Test constructors.
    */
    public void testNormalConstructors()
    {
        new InfoDescriptor(
          m_name, m_classname, m_version, LifestylePolicy.SINGLETON, m_collection,
          m_threadsafe, getProperties() );
        new InfoDescriptor(
          m_name, m_classname, m_version, LifestylePolicy.THREAD, m_collection,
          m_threadsafe, getProperties() );
        new InfoDescriptor(
          m_name, m_classname, m_version, LifestylePolicy.TRANSIENT, m_collection,
          m_threadsafe, getProperties() );
    }
    
   /**
    * Test encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        InfoDescriptor info = getInfoDescriptor();
        executeEncodingTest( info, "info.xml" );
    }
}
