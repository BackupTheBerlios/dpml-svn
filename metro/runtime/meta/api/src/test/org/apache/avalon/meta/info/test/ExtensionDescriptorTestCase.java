/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.meta.info.test;

import org.apache.avalon.meta.info.Descriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;

/**
 * ExtensionDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExtensionDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ExtensionDescriptorTestCase extends AbstractDescriptorTestCase
{
    private String m_key;

    public ExtensionDescriptorTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_key = ExtensionDescriptorTestCase.class.getName();
    }

    protected Descriptor getDescriptor()
    {
        return new ExtensionDescriptor( m_key, getProperties());
    }

    protected void checkDescriptor(Descriptor desc)
    {
        super.checkDescriptor(desc);
        ExtensionDescriptor ext = (ExtensionDescriptor) desc;

        assertEquals( m_key, ext.getKey() );
    }

    public void testConstructor()
    {
        try
        {
            new ExtensionDescriptor(null, getProperties());
            fail("Did not throw the expected NullPointerException");
        }
        catch(NullPointerException npe)
        {
            // Success!!
        }
    }
}
