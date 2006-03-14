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

package net.dpml.state.test;

import net.dpml.state.Operation;
import net.dpml.state.impl.DefaultOperation;

/**
 * DefaultOperationTestCase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultOperationTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private Operation m_operation;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_operation = new DefaultOperation( m_name );
    }
    
   /**
    * Test name accessor.
    * @exception Exception if an error occurs
    */
    public void testName() throws Exception
    {
        assertEquals( "name", m_name, m_operation.getName() );
    }
    
   /**
    * Test that the constructor throws an NPE if supplied with a null name.
    * @exception Exception if an error occurs
    */
    public void testNullName() throws Exception
    {
        try
        {
            new DefaultOperation( null );
            fail( "NO NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test encoding/decoding of an operation instance.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        Operation operation = (Operation) executeEncodingTest( m_operation );
        assertEquals( "original-equals-encoded", m_operation, operation );
    }
}



