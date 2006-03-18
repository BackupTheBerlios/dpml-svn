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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import net.dpml.metro.info.EntryDescriptor;

/**
 * EntryDescriptorTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class EntryDescriptorTestCase extends TestCase
{
    private static final String KEY = "key";
    private static final String TYPE = EntryDescriptor.class.getName();
    private static final boolean OPTIONAL = true;
    private static final boolean VOLATILE = true;

   /**
    * Test an entry descriptor.
    */
    public void testEntryDescriptor()
    {
        EntryDescriptor entry = new EntryDescriptor( KEY, TYPE, OPTIONAL, VOLATILE );
        checkEntry( entry, KEY, TYPE, OPTIONAL, VOLATILE );

        entry = new EntryDescriptor( KEY, TYPE );
        checkEntry( entry, KEY, TYPE, false, false );

        entry = new EntryDescriptor( KEY, TYPE, OPTIONAL );
        checkEntry( entry, KEY, TYPE, OPTIONAL, false );
    }
    
   /**
    * Test that the constructor throws an NPE when supplied with a null key.
    */
    public void testNullKey()
    {
        try
        {
            new EntryDescriptor( null, TYPE );
            fail( "Did not throw expected NullPointerException " );
        }
        catch( NullPointerException npe )
        {
            // Success!!
        }
    }
    
   /**
    * Test that the constructor throws an NPE when supplied with a null type.
    */
    public void testNullType()
    {
        try
        {
            new EntryDescriptor( KEY, null );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }
    }
    
   /**
    * Validate the entry descriptor.
    * @param desc the entry descriptor to validate
    * @param key the entry key
    * @param type the entry type
    * @param isOptional the optional flag
    * @param isVolatile the volotile flag
    */
    private void checkEntry( 
      EntryDescriptor desc, String key, String type, boolean isOptional, boolean isVolatile )
    {
        assertNotNull( desc );
        assertEquals( key, desc.getKey() );
        assertEquals( type, desc.getClassname() );
        assertEquals( isOptional, desc.isOptional() );
        assertEquals( !isOptional, desc.isRequired() );
        assertEquals( isVolatile, desc.isVolatile() );
    }

   /**
    * Test entry serialization.
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if a class is not found
    */
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        EntryDescriptor entry = new EntryDescriptor( KEY, TYPE, OPTIONAL, VOLATILE );
        checkEntry( entry, KEY, TYPE, OPTIONAL, VOLATILE );

        File file = new File( "test.out" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( entry );
        oos.close();
        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        EntryDescriptor serialized = (EntryDescriptor) ois.readObject();
        ois.close();
        file.delete();
        checkEntry( serialized, KEY, TYPE, OPTIONAL, VOLATILE );
        assertEquals( entry, serialized );
        assertEquals( entry.hashCode(), serialized.hashCode() );
    }
    
}
