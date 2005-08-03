/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.test;

import junit.framework.TestCase;
import net.dpml.magic.AntFileIndex;
import net.dpml.magic.project.Context;
import org.apache.tools.ant.Project;


/**
 * IndexTestCase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class IndexTestCase extends TestCase
{
    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
    }

    private AntFileIndex m_index;

    public void setUp()
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        m_index = context.getIndex();
    }

    public void testIndex() throws Exception
    {
        // TODO:
        assertEquals( m_index, m_index );
    }

}
