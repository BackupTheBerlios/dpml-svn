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

import net.dpml.metro.info.PartReference;

import net.dpml.component.Directive;
import net.dpml.metro.data.ValueDirective;

/**
 * PartReferenceTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartReferenceTestCase extends AbstractEncodingTestCase
{
    private static final String KEY = "key";
    private static final Directive PART = new ValueDirective( "abc" );
    
   /**
    * Test the directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        PartReference ref = new PartReference( KEY, PART );
        executeEncodingTest( ref );
    }
}
