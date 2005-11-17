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

import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;

import net.dpml.metro.info.Descriptor;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;

import net.dpml.metro.part.Version;

/**
 * InfoDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class InfoDescriptorTestCase extends AbstractDescriptorTestCase
{
    private final String m_name = "name";
    private final String m_classname = InfoDescriptorTestCase.class.getName();
    private final Version m_version = Version.getVersion("1.2.3");
    private final LifestylePolicy m_lifestyle = LifestylePolicy.SINGLETON;
    private final CollectionPolicy m_collection = CollectionPolicy.WEAK;
    private final boolean m_threadsafe = false;
    
    private InfoDescriptor m_info;
    
    public void setUp() throws Exception
    {
        m_info = getInfoDescriptor();
    }
    
    public void testName()
    {
        assertEquals( m_name, m_info.getName() );
    }
    
    public void testClassName()
    {
        assertEquals( m_classname, m_info.getClassname() );
    }
    
    public void testVersion()
    {
        assertEquals( m_version, m_info.getVersion() );
    }
    
    public void testLifestyle()
    {
        assertEquals( m_lifestyle, m_info.getLifestyle() );
    }
    
    public void testThreadsafeCapable()
    {
        assertEquals( m_threadsafe, m_info.isThreadsafe() );
    }
    
    public void testCollectionPolicy()
    {
        assertEquals( m_collection, m_info.getCollectionPolicy() );
    }
    
    protected Descriptor getDescriptor()
    {
        return getInfoDescriptor();
    }
    
    protected InfoDescriptor getInfoDescriptor()
    {
        return new InfoDescriptor(
          m_name, m_classname, m_version, m_lifestyle, m_collection, 
          m_threadsafe, getProperties());
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        InfoDescriptor info = (InfoDescriptor) desc;
        assertEquals( m_name, info.getName() );
        assertEquals( m_classname, info.getClassname() );
        assertEquals( m_version, info.getVersion() );
        assertEquals( m_lifestyle, info.getLifestyle() );
        assertEquals( m_threadsafe, info.isThreadsafe() );
        assertEquals( m_collection, info.getCollectionPolicy() );
    }

    public void testNullClassnameConstructor()
    {
        try
        {
            new InfoDescriptor(
              m_name, null, m_version, m_lifestyle, m_collection, 
              m_threadsafe, getProperties() );
            fail("Did not throw a NullPointerException");
        }
        catch( NullPointerException npe )
        {
            // Success!
        }
    }
    
    public void testBadClassnameConstructor()
    {
        try
        {
            new InfoDescriptor(
              m_name, "foo/fake/ClassName", m_version, m_lifestyle, m_collection,
              m_threadsafe, getProperties());
            fail("Did not throw the proper IllegalArgumentException");
        }
        catch( IllegalArgumentException iae )
        {
            // Success!
        }
    }
    
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
    
    public void testEncoding() throws Exception
    {
        InfoDescriptor info = getInfoDescriptor();
        executeEncodingTest( info, "info.xml" );
    }
}
