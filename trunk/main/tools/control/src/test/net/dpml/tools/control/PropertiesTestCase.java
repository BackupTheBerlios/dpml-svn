/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.control;

import java.io.File;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Library;
import net.dpml.tools.info.Scope;

import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test the DefaultLibrary implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PropertiesTestCase extends AbstractTestCase
{   
    private Library m_library;
    
    public void setUp() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "library.xml" );
        LoggingAdapter logger = new LoggingAdapter( "library" );
        m_library = DefaultLibrary.load( logger, example );
    }
    
    public void testProperty() throws Exception
    {
        Project project = m_library.getProject( "dpml/runtime/dpml-state-impl" );
        System.out.println( "# PATH " + project.getPath() );
        String value = project.getProperty( "build.template" );
        System.out.println( "# VALUE " + value );
    }
}
