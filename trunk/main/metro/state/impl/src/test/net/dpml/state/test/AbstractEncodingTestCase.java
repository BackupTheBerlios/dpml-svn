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

package net.dpml.state.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.DefaultPersistenceDelegate;
import java.net.URI;

import junit.framework.TestCase;

/**
 * AbstractEncodingTestCase.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public abstract class AbstractEncodingTestCase extends TestCase
{
   /**
    * Utility operation to validate encoding and decoding of a supplied object.
    * @param object the object to encode and decode
    * @return the object resulting from deserialization
    * @exception Exception if an error occurs
    */
    public Object executeEncodingTest( Object object ) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( object );
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Object serialized = ois.readObject();
        ois.close();

        assertTrue( object != serialized ); // Ensure this is not the same instance
        assertEquals( object, serialized );
        assertEquals( object.hashCode(), serialized.hashCode() );
        
        return serialized;
    }

}
