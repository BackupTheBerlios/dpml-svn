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

import junit.framework.TestCase;

import net.dpml.metro.part.Part;
import net.dpml.metro.info.PartReference;

import net.dpml.metro.data.ValueDirective;

import net.dpml.transit.model.Construct;

/**
 * EntryDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class PartReferenceTestCase extends AbstractEncodingTestCase
{
    private static final String m_key = "key";
    private static final Part m_part = new ValueDirective( "abc" );
    
    public void testEncoding() throws Exception
    {
        PartReference ref = new PartReference( m_key, m_part );
        executeEncodingTest( ref, "part-reference.xml" );
    }
}
