/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.state;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

/**
 * Default state machine test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class EmptyTestCase extends TestCase
{
    private StateDecoder m_decoder;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_decoder = new StateDecoder();
    }
    
   /**
    * List the state graph.
    * @exception Exception if an error occurs
    */
    public void testStateLoading() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "empty.xml" );
        URI uri = example.toURI();
        State state = m_decoder.loadState( uri );
        assertEquals( "substates", 0, state.getStates().length );
        assertEquals( "transitions", 0, state.getTransitions().length );
        assertEquals( "triggers", 0, state.getTriggers().length );
    }
}



