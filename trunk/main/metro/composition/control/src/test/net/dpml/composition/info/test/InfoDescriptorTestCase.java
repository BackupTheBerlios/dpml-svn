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
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.info.Version;

/**
 * InfoDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class InfoDescriptorTestCase extends AbstractDescriptorTestCase
{
    private final String m_name = "name";
    private final String m_classname = InfoDescriptorTestCase.class.getName();
    private final Version m_version = Version.getVersion("1.2.3");
    private final String m_lifestyle = InfoDescriptor.SINGLETON;
    private final String m_collection = InfoDescriptor.WEAK;
    private final String m_schema = "schema";
    private final boolean m_native = true;
    private final boolean m_threadsafe = false;

    public InfoDescriptorTestCase ( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new InfoDescriptor(
          m_name, m_classname, m_version, m_lifestyle, m_collection, m_schema, m_threadsafe, m_native, getProperties());
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        InfoDescriptor info = (InfoDescriptor) desc;
        assertEquals( m_name, info.getName() );
        assertEquals( m_classname, info.getClassname() );
        assertEquals( m_version, info.getVersion() );
        assertEquals( m_lifestyle, info.getLifestyle() );
        assertEquals( m_threadsafe, info.isThreadsafe() );
        assertEquals( InfoDescriptor.getCollectionPolicy( m_collection ), info.getCollectionPolicy() );
        assertEquals( m_schema, info.getConfigurationSchema() );
    }

    public void testConstructor()
    {
        try
        {
            new InfoDescriptor(
              m_name, null, m_version, m_lifestyle, m_collection, m_schema, m_threadsafe, m_native, getProperties() );
            fail("Did not throw the proper NullPointerException");
        }
        catch( NullPointerException npe )
        {
            // Success!
        }

        try
        {
            new InfoDescriptor(
              m_name, "foo/fake/ClassName", m_version, m_lifestyle, m_collection,
              m_schema, m_threadsafe, m_native, getProperties());
            fail("Did not throw the proper IllegalArgumentException");
        }
        catch( IllegalArgumentException iae )
        {
            // Success!
        }

        try
        {
            new InfoDescriptor(
              m_name, m_classname, m_version, InfoDescriptor.SINGLETON, m_collection,
              m_schema, m_threadsafe, m_native, getProperties() );
            new InfoDescriptor(
              m_name, m_classname, m_version, InfoDescriptor.THREAD, m_collection,
              m_schema, m_threadsafe, m_native, getProperties() );
            new InfoDescriptor(
              m_name, m_classname, m_version, InfoDescriptor.TRANSIENT, m_collection,
              m_schema, m_threadsafe, m_native, getProperties() );

            // All these should pass.

            new InfoDescriptor(
              m_name, m_classname, m_version, "Fake Lifestyle", m_collection,
              m_schema, m_threadsafe, m_native, getProperties() );

            fail( "Did not throw the proper IllegalArgumentException" );
        }
        catch ( IllegalArgumentException iae )
        {
            // Success!
        }
    }
}
