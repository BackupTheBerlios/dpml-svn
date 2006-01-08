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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import net.dpml.metro.info.Descriptor;

/**
 * AbstractDescriptorTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractDescriptorTestCase extends AbstractEncodingTestCase
{
   /**
    * Key.
    */
    protected static final String VALID_KEY = "key";
    
   /**
    * Value.
    */
    protected static final String VALID_VALUE = "value";
    
   /**
    * Bad key.
    */
    protected static final String INVALID_KEY = "bad-key";
    
   /**
    * Default value.
    */
    protected static final String DEFAULT_VALUE = "default";

   /**
    * Return a populated properties object.
    * @return a properties instance
    */
    protected Properties getProperties()
    {
        Properties props = new Properties();
        props.put( VALID_KEY, VALID_VALUE );

        return props;
    }

   /**
    * Abstract operation implemented by derived types.
    * @return the descriptor to test
    */
    protected abstract Descriptor getDescriptor();

   /**
    * Validate the supplied descriptor.
    * @param desc the descriptor to validate
    */
    protected void checkDescriptor( Descriptor desc )
    {
        assertEquals( VALID_VALUE, desc.getAttribute( VALID_KEY ) );
        assertEquals( DEFAULT_VALUE, desc.getAttribute( INVALID_KEY, DEFAULT_VALUE ) );

        boolean hasValid = false;
        boolean hasInvalid = false;
        String[] names = desc.getAttributeNames();

        assertNotNull( names );
        assertTrue( names.length > 0 );

        for ( int i = 0; i < names.length; i++ )
        {
            if ( VALID_KEY.equals( names[i] ) )
            {
                hasValid = true;
            }

            if ( INVALID_KEY.equals( names[i] ) )
            {
                hasInvalid = true;
            }
        }

        assertTrue( hasValid );
        assertTrue( !hasInvalid );
    }

   /**
    * Test serialization of the object retutrned from getDescriptor().
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        Descriptor desc = getDescriptor();
        checkDescriptor( desc );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( desc );
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Descriptor serialized = (Descriptor) ois.readObject();
        ois.close();

        assertTrue( desc != serialized ); // Ensure this is not the same instance
        checkDescriptor( serialized );

        assertEquals( desc, serialized );
        assertEquals( desc.hashCode(), serialized.hashCode() );
    }
}
