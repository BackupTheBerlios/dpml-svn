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

import org.apache.avalon.meta.info.*;

/**
 * ServiceDescriptorTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: StageDescriptorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class StageDescriptorTestCase extends AbstractDescriptorTestCase
{
    private String m_key;

    public StageDescriptorTestCase( String name )
    {
        super( name );
    }

    protected Descriptor getDescriptor()
    {
        return new StageDescriptor(m_key, getProperties());
    }

    public void setUp()
    {
        m_key = StageDescriptorTestCase.class.getName();
    }


    public void testConstructor()
    {
        try
        {
            new StageDescriptor( null, getProperties() );
            fail( "Did not throw the expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Sucess!
        }
    }

    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        StageDescriptor stage = (StageDescriptor) desc;

        assertEquals( m_key, stage.getKey() );
    }
}
