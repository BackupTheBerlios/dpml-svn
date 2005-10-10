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

package net.dpml.component.info.test;

import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;

import net.dpml.component.info.Descriptor;
import net.dpml.component.info.ContextDescriptor;

import net.dpml.composition.AbstractEncodingTestCase;
import net.dpml.component.info.EntryDescriptor;

import junit.framework.TestCase;

/**
 * ContextDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ContextDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class ContextDescriptorTestCase extends AbstractEncodingTestCase
{
    private EntryDescriptor[] m_entries;

    protected ContextDescriptor getDescriptor()
    {
        return new ContextDescriptor( m_entries );
    }

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
        m_entries = new EntryDescriptor[]{
            new EntryDescriptor( "key", String.class.getName() )
        };
    }
    
    public void testEncoding() throws Exception
    {
        ContextDescriptor context = getDescriptor();
        executeEncodingTest( context, "context.xml" );
    }
}
