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
import org.apache.avalon.meta.info.EntryDescriptor;

import java.io.*;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: EntryDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class EntryDescriptorTestCase extends TestCase
{
    private static final String m_key = "key";
    private static final String m_alias = "otherVal";
    private static final String m_type = EntryDescriptor.class.getName();
    private static final boolean m_optional = true;
    private static final boolean m_volatile = true;

    public EntryDescriptorTestCase( String name )
    {
        super( name );
    }

    public void testEntryDescriptor()
    {
        EntryDescriptor entry = new EntryDescriptor(m_key, m_type, m_optional, m_volatile, m_alias);
        checkEntry(entry, m_key, m_type, m_optional, m_volatile, m_alias );

        entry = new EntryDescriptor(m_key, m_type);
        checkEntry(entry, m_key, m_type, false, false, null );

        entry = new EntryDescriptor(m_key, m_type, m_optional);
        checkEntry(entry, m_key, m_type, m_optional, false, null );

        entry = new EntryDescriptor( m_key, m_type, m_optional, m_volatile );
        checkEntry( entry, m_key, m_type, m_optional, m_volatile, null );

        try
        {
            new EntryDescriptor(null, m_type);
            fail("Did not throw expected NullPointerException");
        }
        catch(NullPointerException npe)
        {
            // Success!!
        }

        try
        {
            new EntryDescriptor( m_key, null );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }
    }

    private void checkEntry(EntryDescriptor desc, String key, String type, boolean isOptional, boolean isVolatile, String alias)
    {
        assertNotNull( desc );
        assertEquals( key, desc.getKey() );
        if( alias == null )
        {
            assertEquals( key, desc.getAlias() );
        }
        else
        {
            assertEquals( alias, desc.getAlias() );
        }
        assertEquals( type, desc.getClassname() );
        assertEquals( isOptional, desc.isOptional() );
        assertEquals( ! isOptional, desc.isRequired() );
        assertEquals( isVolatile, desc.isVolatile() );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        EntryDescriptor entry = new EntryDescriptor( m_key, m_type, m_optional, m_volatile, m_alias );
        checkEntry( entry, m_key, m_type, m_optional, m_volatile, m_alias );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( entry );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        EntryDescriptor serialized = (EntryDescriptor) ois.readObject();
        ois.close();
        file.delete();

        checkEntry( serialized, m_key, m_type, m_optional, m_volatile, m_alias );

        assertEquals( entry, serialized );
        assertEquals( entry.hashCode(), serialized.hashCode() );
    }
}
