/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package net.dpml.transit.test;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

/**
 * AbstractEncodingTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractEncodingTestCase extends TestCase
{
   /**
    * Utility operation to encode and decode an supplied object using an intermidiate file.
    * @param object the object to enciode
    * @param filename the intermidiate filename resolved relative to the target/test directory
    * @return the result of decoding the encoded structure
    * @exception Exception if an error occurs
    */
    public Object executeEncodingTest( Object object ) throws Exception
    {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream( byteOutputStream );
        output.writeObject( object );
        output.close();

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream( byteOutputStream.toByteArray() );
        ObjectInputStream input = new ObjectInputStream( byteInputStream );
        Object serialized = input.readObject();
        input.close();

        assertTrue( "!=", object != serialized ); // Ensure this is not the same instance
        assertEquals( "equals", object, serialized );
        assertEquals( "hash", object.hashCode(), serialized.hashCode() );
        
        return serialized;
    }

}
