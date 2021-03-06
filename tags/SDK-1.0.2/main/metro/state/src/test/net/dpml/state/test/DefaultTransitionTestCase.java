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
import net.dpml.state.Transition;
import net.dpml.state.DefaultTransition;
import net.dpml.state.DefaultOperation;

/**
 * Default transition test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTransitionTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private Operation m_operation;
    private String m_target;
    private Transition m_transition;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_operation = new DefaultOperation( "xyz" );
        m_target = "somewhere";
        m_transition = new DefaultTransition( m_name, m_target, m_operation );
    }
    
   /**
    * Test name accessor.
    * @exception Exception if an error occurs
    */
    public void testName() throws Exception
    {
        assertEquals( "name", m_name, m_transition.getName() );
    }
    
   /**
    * Test target name accessor.
    * @exception Exception if an error occurs
    */
    public void testTargetName() throws Exception
    {
        assertEquals( "target", m_target, m_transition.getTargetName() );
    }
    
   /**
    * Test operation accessor.
    * @exception Exception if an error occurs
    */
    public void testOperation() throws Exception
    {
        assertEquals( "operation", m_operation, m_transition.getOperation() );
    }
    
   /**
    * Test transition encoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        Transition transition = (Transition) executeEncodingTest( m_transition );
        assertEquals( "original-equals-encoded", m_transition, transition );
    }
    
   /**
    * Test null constructor arguments.
    * @exception Exception if an error occurs
    */
    public void testNulls() throws Exception
    {
        try
        {
            new DefaultTransition( null, null, null );
            fail( "NPE not thown" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
    
   /**
    * Test null name constructor arguments.
    * @exception Exception if an error occurs
    */
    public void testNullName() throws Exception
    {
        try
        {
            new DefaultTransition( null, m_target, m_operation );
            fail( "NPE not thown for null name" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
    
   /**
    * Test null target constructor arguments.
    * @exception Exception if an error occurs
    */
    public void testNullTarget() throws Exception
    {
        try
        {
            new DefaultTransition( m_name, null, m_operation );
            fail( "NPE not thown for null target" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
    
   /**
    * Test allowable null operation constructor arguments.
    * @exception Exception if an error occurs
    */
    public void testNullOperation() throws Exception
    {
        try
        {
            new DefaultTransition( m_name, m_target, null );
        }
        catch( NullPointerException e )
        {
            fail( "Null operation is allowed." );
        }
    }
}



