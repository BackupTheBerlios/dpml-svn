/* 
 * Copyright 2004 Stephen J. McConnell.
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

import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.EntryDescriptor;

/**
 * ContextDescriptorTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextDescriptorTestCase extends AbstractEncodingTestCase
{
    private EntryDescriptor[] m_entries;

   /**
    * Return the descriptor to test.
    * @return the descriptor
    */
    protected ContextDescriptor getDescriptor()
    {
        return new ContextDescriptor( m_entries );
    }

   /**
    * Validate the descriptor.
    * @param desc the descriptor to validate
    */
    protected void checkDescriptor( ContextDescriptor desc )
    {
        ContextDescriptor ctxd = (ContextDescriptor) desc;

        assertEquals( m_entries.length, ctxd.getEntryDescriptors().length );

        EntryDescriptor[] entries = ctxd.getEntryDescriptors();

        for ( int i = 0; i < m_entries.length; i++ )
        {
            assertEquals( m_entries[i], entries[i] );
            assertEquals( m_entries[i], ctxd.getEntryDescriptor( m_entries[i].getKey() ) );
        }
    }

   /**
    * Test join.
    */
    public void testJoin()
    {
        ContextDescriptor desc = (ContextDescriptor) getDescriptor();
        EntryDescriptor[] good = new EntryDescriptor[]{
            new EntryDescriptor( "key", String.class.getName() ),
            new EntryDescriptor( "no conflict", String.class.getName() )
        };
        EntryDescriptor[] bad = new EntryDescriptor[]{
            new EntryDescriptor( "key", Integer.class.getName() )
        };

        checkDescriptor( desc );
        EntryDescriptor[] merged = desc.merge( good );
        checkDescriptor( desc );

        // The items to merge in are first.  Shouldn't this be a set?
        assertEquals( good[0], merged[0] );
        assertEquals( good[1], merged[1] );
        assertEquals( m_entries[0], merged[2] );

        try
        {
            desc.merge( bad );
            fail( "Did not throw expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException iae )
        {
            // Success!!
        }
    }

   /**
    * Setup the testcase.
    */
    public void setUp()
    {
        m_entries = new EntryDescriptor[]{
            new EntryDescriptor( "key", String.class.getName() )
        };
    }
    
   /**
    * Test context descriptor encoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        ContextDescriptor context = getDescriptor();
        executeEncodingTest( context, "context.xml" );
    }
}
