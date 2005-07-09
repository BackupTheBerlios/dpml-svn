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
import net.dpml.composition.info.Service;
import net.dpml.composition.info.ReferenceDescriptor;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.Version;

/**
 * ServiceTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ServiceTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class ServiceTestCase extends AbstractDescriptorTestCase
{
    private ReferenceDescriptor m_reference;
    private EntryDescriptor[] m_entries;

    public ServiceTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new Service(m_reference, m_entries, getProperties());
    }

    public void setUp()
    {
        m_reference = new ReferenceDescriptor(ServiceTestCase.class.getName(), Version.getVersion("1.2.3"));
        m_entries = new EntryDescriptor[] {
            new EntryDescriptor("key", String.class.getName())
        };
    }

    public void testConstructor()
    {
        try
        {
            new Service(null);
            fail("Did not throw the expected NullPointerException");
        }
        catch( NullPointerException npe )
        {
            // Success!
        }
    }

    protected void checkDescriptor(Descriptor desc)
    {
        super.checkDescriptor(desc);
        Service service = (Service)desc;

        assertEquals( m_reference, service.getReference());
        assertEquals( m_reference.getClassname(), service.getClassname());
        assertEquals( m_reference.getVersion(), service.getVersion());

        assertEquals( m_entries.length, service.getEntries().length );
        assertTrue( service.matches(m_reference));

        EntryDescriptor[] serviceEntries = service.getEntries();
        for (int i = 0; i < m_entries.length; i++)
        {
            assertEquals( m_entries[i], serviceEntries[i]);
        }
    }
}
