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
import net.dpml.composition.info.DependencyDescriptor;
import net.dpml.composition.info.ReferenceDescriptor;
import net.dpml.composition.info.Version;

/**
 * DependencyDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: DependencyDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class DependencyDescriptorTestCase extends AbstractDescriptorTestCase
{
    private String m_role;
    private ReferenceDescriptor m_reference;
    private boolean m_optional = true;

    public DependencyDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new DependencyDescriptor( m_role, m_reference, m_optional, getProperties() );
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );

        DependencyDescriptor dep = (DependencyDescriptor) desc;
        assertEquals( m_role, dep.getKey() );
        assertEquals( m_reference, dep.getReference() );
        assertEquals( m_optional, dep.isOptional() );
        assertEquals( !m_optional, dep.isRequired() );
    }

    public void setUp()
    {
        m_role = "Test";
        m_reference = new ReferenceDescriptor( DependencyDescriptorTestCase.class.getName(), Version.getVersion( "1.2.1" ) );
    }

}
