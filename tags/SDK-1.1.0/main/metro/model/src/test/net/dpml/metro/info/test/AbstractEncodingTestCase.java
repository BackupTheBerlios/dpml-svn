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

package net.dpml.metro.info.test;

import junit.framework.TestCase;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractEncodingTestCase extends TestCase
{
   /**
    * Test encoding of an object.
    * @param object the object to encode
    * @param filename path relative to the target/test directory
    * @return the result of decoding an encoded representation of object
    * @exception Exception if an error occurs
    */
    public Object executeEncodingTest( Object object, String filename ) throws Exception
    {
        return object;
    }
}
