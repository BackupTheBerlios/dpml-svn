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

package net.dpml.composition.info.test;

import net.dpml.composition.info.Descriptor;
import net.dpml.composition.info.ContextDescriptor;
import net.dpml.composition.info.EntryDescriptor;

/**
 * ContextDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ContextDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class ContextDescriptorTestCase extends AbstractDescriptorTestCase
{
    private String m_classname;
    private EntryDescriptor[] m_entries;

    public ContextDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new ContextDescriptor( m_classname, m_entries, getProperties() );
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        ContextDescriptor ctxd = (ContextDescriptor) desc;

        assertEquals( m_classname, ctxd.getContextInterfaceClassname() );
        assertEquals( m_entries.length, ctxd.getEntries().length );

        EntryDescriptor[] entries = ctxd.getEntries();

        for ( int i = 0; i < m_entries.length; i++ )
        {
            assertEquals( m_entries[i], entries[i] );
            assertEquals( m_entries[i], ctxd.getEntry( m_entries[i].getKey() ) );
        }
    }

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

    public void setUp()
    {
        m_classname = "org.apache.avalon.playground.MyContext";
        m_entries = new EntryDescriptor[]{
            new EntryDescriptor( "key", String.class.getName() )
        };
    }
}
