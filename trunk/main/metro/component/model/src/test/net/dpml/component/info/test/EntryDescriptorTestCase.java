/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;

import junit.framework.TestCase;

import net.dpml.metro.info.EntryDescriptor;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class EntryDescriptorTestCase extends TestCase
{
    private static final String m_key = "key";
    private static final String m_type = EntryDescriptor.class.getName();
    private static final boolean m_optional = true;
    private static final boolean m_volatile = true;

    public EntryDescriptorTestCase( String name )
    {
        super( name );
    }

    public void testEntryDescriptor()
    {
        EntryDescriptor entry = new EntryDescriptor( m_key, m_type, m_optional, m_volatile );
        checkEntry( entry, m_key, m_type, m_optional, m_volatile );

        entry = new EntryDescriptor( m_key, m_type );
        checkEntry( entry, m_key, m_type, false, false );

        entry = new EntryDescriptor(m_key, m_type, m_optional );
        checkEntry( entry, m_key, m_type, m_optional, false );

        try
        {
            new EntryDescriptor( null, m_type );
            fail("Did not throw expected NullPointerException ");
        }
        catch( NullPointerException npe)
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

    private void checkEntry( EntryDescriptor desc, String key, String type, boolean isOptional, boolean isVolatile )
    {
        assertNotNull( desc );
        assertEquals( key, desc.getKey() );
        assertEquals( type, desc.getClassname() );
        assertEquals( isOptional, desc.isOptional() );
        assertEquals( ! isOptional, desc.isRequired() );
        assertEquals( isVolatile, desc.isVolatile() );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        EntryDescriptor entry = new EntryDescriptor( m_key, m_type, m_optional, m_volatile );
        checkEntry( entry, m_key, m_type, m_optional, m_volatile );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( entry );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        EntryDescriptor serialized = (EntryDescriptor) ois.readObject();
        ois.close();
        file.delete();

        checkEntry( serialized, m_key, m_type, m_optional, m_volatile );

        assertEquals( entry, serialized );
        assertEquals( entry.hashCode(), serialized.hashCode() );
    }
    
    public void testEncoding() throws Exception
    {
        EntryDescriptor entry = new EntryDescriptor( m_key, m_type, m_optional, m_volatile );
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File destination = new File( test, "info.xml" );
        FileOutputStream output = new FileOutputStream( destination );
        BufferedOutputStream buffer = new BufferedOutputStream( output );
        XMLEncoder encoder = new XMLEncoder( buffer );
        encoder.setExceptionListener( 
          new ExceptionListener()
          {
            public void exceptionThrown( Exception e )
            {
                e.printStackTrace();
                fail( "encoding exception: " + e.toString() );
            }
          }
        );
        encoder.writeObject( entry );
        encoder.close();
        
        FileInputStream input = new FileInputStream( destination );
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
        EntryDescriptor result = (EntryDescriptor) decoder.readObject();
        assertEquals( "encoding", entry, result );
    }

}
