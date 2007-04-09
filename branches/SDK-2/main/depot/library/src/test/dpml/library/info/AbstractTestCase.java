/*
 * Copyright 2005 Stephen J. McConnell
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

package dpml.library.info;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * AbstractTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class AbstractTestCase extends TestCase
{
    public static final Properties PROPERTIES = new Properties();
    
    public void doSerializationTest( Object object )
        throws Exception
    {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream( byteOutputStream );
        output.writeObject( object );
        output.close();

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream( byteOutputStream.toByteArray() );
        ObjectInputStream input = new ObjectInputStream( byteInputStream );
        Object serialized = input.readObject();
        input.close();

        assertTrue( object != serialized ); // Ensure this is not the same instance
        assertEquals( object, serialized );
        assertEquals( object.hashCode(), serialized.hashCode() );
    }
}
