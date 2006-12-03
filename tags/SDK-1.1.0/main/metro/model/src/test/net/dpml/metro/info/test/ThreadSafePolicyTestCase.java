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

import net.dpml.metro.info.ThreadSafePolicy;

import junit.framework.TestCase;

/**
 * ThreadSafePolicy testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ThreadSafePolicyTestCase extends TestCase
{
   /**
    * Test weak policy.
    * @exception Exception if an error occurs
    */
    public void testThreadSafePolicy() throws Exception
    {
        ThreadSafePolicy policy = ThreadSafePolicy.parse( "true" );
        assertEquals( "name", "true", policy.getName() );
    }

   /**
    * Test soft policy.
    * @exception Exception if an error occurs
    */
    public void testNonThreadThreadSafePolicy() throws Exception
    {
        ThreadSafePolicy policy = ThreadSafePolicy.parse( "false" );
        assertEquals( "name", "false", policy.getName() );
    }

   /**
    * Test hard policy.
    * @exception Exception if an error occurs
    */
    public void testUnknownThreadSafePolicy() throws Exception
    {
        ThreadSafePolicy policy = ThreadSafePolicy.parse( "unknown" );
        assertEquals( "name", "unknown", policy.getName() );
    }
    

   /**
    * Test pad policy.
    * @exception Exception if an error occurs
    */
    public void testBadPolicy() throws Exception
    {
        try
        {
            ThreadSafePolicy policy = ThreadSafePolicy.parse( "???" );
            fail( "Bad thread-safe policy argument succeeded." );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }
}
