/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.meta.info.test;

import junit.framework.TestCase;
import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.ReferenceDescriptor;

import java.io.*;

/**
 * ReferenceDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ReferenceDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ReferenceDescriptorTestCase extends TestCase
{
    private static final String m_classname = ReferenceDescriptorTestCase.class.getName();
    private static final Version m_version = Version.getVersion("1.2.3");

    public ReferenceDescriptorTestCase( String name )
    {
        super( name );
    }

    public void testConstructor()
    {
        try
        {
            new ReferenceDescriptor(null, m_version);
            fail("Did not throw the expected NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Success!
        }

        try
        {
            new ReferenceDescriptor( null );
            fail( "Did not throw the expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!
        }

        ReferenceDescriptor ref = new ReferenceDescriptor( m_classname, m_version );
        checkDescriptor( ref, m_classname, m_version );

        ref = new ReferenceDescriptor( m_classname + ":3.2.1" );
        checkDescriptor( ref, m_classname, Version.getVersion("3.2.1"));
    }

    private void checkDescriptor(ReferenceDescriptor ref, String classname, Version version)
    {
        assertNotNull(ref);
        assertNotNull(ref.getClassname());
        assertEquals(classname, ref.getClassname());
        assertNotNull(ref.getVersion());
        assertEquals(version, ref.getVersion());
    }

    public void testCompliance()
    {
        ReferenceDescriptor ref = new ReferenceDescriptor( m_classname, m_version );
        ReferenceDescriptor any = new ReferenceDescriptor( m_classname, new Version( -1, 0, 0 ) );

        assertTrue( "anything matches explicit", any.matches( ref ) );
        assertFalse( "explicit does not match anything", ref.matches( any ) );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        ReferenceDescriptor entry = new ReferenceDescriptor( m_classname, m_version );
        checkDescriptor( entry, m_classname, m_version );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( entry );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        ReferenceDescriptor serialized = (ReferenceDescriptor) ois.readObject();
        ois.close();
        file.delete();

        checkDescriptor( entry, m_classname, m_version );

        assertEquals( entry, serialized );
        assertEquals( entry.hashCode(), serialized.hashCode() );
    }
}
