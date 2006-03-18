/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.data.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    * Execution of an encoding test.
    * @param object the object encode/decode
    * @return the decoded object
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
