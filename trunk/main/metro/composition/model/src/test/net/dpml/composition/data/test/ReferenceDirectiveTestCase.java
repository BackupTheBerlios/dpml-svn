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

import java.net.URI;

import net.dpml.composition.AbstractEncodingTestCase;
import net.dpml.composition.data.ReferenceDirective;

/**
 * Test encoding of the reference directive.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ReferenceDirectiveTestCase extends AbstractEncodingTestCase
{
    public void testEncoding() throws Exception
    {
        URI uri = new URI( "lookup:anything" ) ;
        ReferenceDirective value = new ReferenceDirective( uri );
        executeEncodingTest( value, "reference-directive.xml" );
    }
}
