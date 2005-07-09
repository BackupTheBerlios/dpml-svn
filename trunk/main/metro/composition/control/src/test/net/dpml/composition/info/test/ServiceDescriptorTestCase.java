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
import net.dpml.composition.info.ReferenceDescriptor;
import net.dpml.composition.info.ServiceDescriptor;
import net.dpml.composition.info.Version;

/**
 * ServiceDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ServiceDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class ServiceDescriptorTestCase extends AbstractDescriptorTestCase
{
    private ReferenceDescriptor m_designator;

    public ServiceDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new ServiceDescriptor(m_designator, getProperties());
    }

    public void setUp()
    {
        m_designator = new ReferenceDescriptor( ServiceTestCase.class.getName(), Version.getVersion( "1.2.3" ) );
    }


    public void testConstructor()
    {
        try
        {
            new ServiceDescriptor( null, getProperties() );
            fail( "Did not throw the expected NullPointerException" );
        }
        catch( NullPointerException npe )
        {
            // Success!
        }
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        ServiceDescriptor service = (ServiceDescriptor) desc;

        assertEquals( m_designator, service.getReference() );
    }
}
