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

import net.dpml.metro.info.Descriptor;
import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.Priority;


/**
 * CategoryTestCase does XYZ
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoryDescriptorTestCase extends AbstractDescriptorTestCase
{
    private final String m_name = "name";
    private final Priority m_priority = Priority.WARN;

    protected CategoryDescriptor getCategoryDescriptor()
    {
        return new CategoryDescriptor( m_name, m_priority, getProperties() );
    }

    protected Descriptor getDescriptor()
    {
        return getCategoryDescriptor();
    }


    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        CategoryDescriptor cat = (CategoryDescriptor) desc;
        assertEquals( "name", m_name, cat.getName() );
        assertEquals( "priority", m_priority, cat.getDefaultPriority() );
    }
    
    public void testEncoding() throws Exception
    {
        CategoryDescriptor descriptor = getCategoryDescriptor();
        executeEncodingTest( descriptor, "category.xml" );
    }
}
