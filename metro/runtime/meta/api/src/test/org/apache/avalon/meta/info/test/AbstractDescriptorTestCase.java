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

import org.apache.avalon.meta.info.Descriptor;

import java.io.*;
import java.util.Properties;

import junit.framework.TestCase;


/**
 * AbstractDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class AbstractDescriptorTestCase extends TestCase
{
    protected static final String VALID_KEY = "key";
    protected static final String VALID_VALUE = "value";
    protected static final String INVALID_KEY = "bad-key";
    protected static final String DEFAULT_VALUE = "default";

    public AbstractDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Properties getProperties()
    {
        Properties props = new Properties();
        props.put( VALID_KEY, VALID_VALUE );

        return props;
    }

    protected abstract Descriptor getDescriptor();

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
            if ( VALID_KEY.equals( names[i] ) ) hasValid = true;
            if ( INVALID_KEY.equals( names[i] ) ) hasInvalid = true;
        }

        assertTrue( hasValid );
        assertTrue( !hasInvalid );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Descriptor desc = getDescriptor();
        checkDescriptor( desc );

        File file = new File( "test.file" );
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( file ) );
        oos.writeObject( desc );
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) );
        Descriptor serialized = (Descriptor) ois.readObject();
        ois.close();
        file.delete();

        assertTrue( desc != serialized ); // Ensure this is not the same instance
        checkDescriptor( serialized );

        assertEquals( desc, serialized );
        assertEquals( desc.hashCode(), serialized.hashCode() );
    }
}
