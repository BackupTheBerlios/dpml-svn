/* 
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.data.test;

import net.dpml.composition.AbstractEncodingTestCase;
import net.dpml.composition.data.ValueDirective;

import junit.framework.TestCase;

/**
 * ValueDirectiveTestCase
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ContextDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class ValueDirectiveTestCase extends AbstractEncodingTestCase
{
    public void testEncoding() throws Exception
    {
        ValueDirective value = new ValueDirective( "test" );
        ValueDirective result = (ValueDirective) executeEncodingTest( value, "simple-value.xml" );
        assertEquals( "encoded-equality", value, result );
        assertEquals( "resolved", "test", value.resolve() );
    }
}
