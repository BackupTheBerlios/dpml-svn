/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.test;

import junit.framework.TestCase;
import net.dpml.magic.UnknownResourceException;


/**
 * UnknownResourceExceptionTestCase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class UnknownResourceExceptionTestCase extends TestCase
{
    public void testNullConstructor() throws Exception
    {
        UnknownResourceException exception = new UnknownResourceException( null );
        assertNotNull( exception.getMessage() );
        assertNull( exception.getKey() );
    }

    public void testKey() throws Exception
    {
        String key = "key";
        UnknownResourceException exception = new UnknownResourceException( key );
        assertEquals( "key", key, exception.getKey() );
    }
}
