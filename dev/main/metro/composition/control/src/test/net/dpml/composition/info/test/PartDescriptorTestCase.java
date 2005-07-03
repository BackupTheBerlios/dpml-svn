/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.info.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import net.dpml.composition.info.PartDescriptor;
import net.dpml.composition.info.PartDescriptor.Operation;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 1874 2005-02-22 17:47:49Z mcconnell $
 */
public class PartDescriptorTestCase extends TestCase
{
    private static final String m_key = "key";
    private static final String m_type = PartDescriptor.class.getName();
    private static final int m_semantic = PartDescriptor.GET;
    private static final Operation m_operation = new Operation( m_semantic, m_type );
    private static final Operation[] m_operations = new Operation[]{ m_operation };

    public void testDescriptor()
    {
        PartDescriptor part = new PartDescriptor( m_key, m_operations );

        assertEquals( "key", m_key, part.getKey() );
        assertEquals( "operations", m_operations, part.getOperations() );

        try
        {
            new PartDescriptor( null, m_operations );
            fail("Did not throw expected NullPointerException ");
        }
        catch( NullPointerException npe)
        {
            // Success!!
        }

        try
        {
            new PartDescriptor( m_key, null );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        PartDescriptor part = new PartDescriptor( m_key, m_operations );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( part );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        PartDescriptor serialized = (PartDescriptor) ois.readObject();
        ois.close();
        file.delete();

        assertEquals( part, serialized );
        assertEquals( part.hashCode(), serialized.hashCode() );
    }
}
