/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ValueDirective;

/**
 * EntryDescriptorTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartReferenceTestCase extends AbstractEncodingTestCase
{
    private final String m_key = "key";
    private ValueDirective m_directive;
    
    private PartReference m_reference;
    
   /**
    * Setup the testcase.
    * @exception Exception if an error occurs during setup.
    */
    public void setUp() throws Exception
    {
        m_directive = new ValueDirective( "test" );
        m_reference = new PartReference( m_key, m_directive );
    }
    
   /**
    * Test key intergrity.
    */
    public void testKey()
    {
        assertEquals( "key", m_key, m_reference.getKey() );
    }
    
   /**
    * Test directive intergrity.
    */
    public void testDirective()
    {
        assertEquals( "directive", m_directive, m_reference.getDirective() );
    }
    
   /**
    * Test part reference encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        PartReference ref = new PartReference( m_key, m_directive );
        executeEncodingTest( ref, "ref.xml" );
    }
    
}
