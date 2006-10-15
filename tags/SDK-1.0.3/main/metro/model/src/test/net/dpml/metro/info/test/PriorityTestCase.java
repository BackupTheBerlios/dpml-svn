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

import net.dpml.metro.info.Priority;

import junit.framework.TestCase;

/**
 * Priority testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PriorityTestCase extends TestCase
{
   /**
    * Test info priority.
    * @exception Exception if an error occurs
    */
    public void testInfoPriority() throws Exception
    {
        String name = "info";
        Priority policy = Priority.parse( name );
        assertEquals( "name", name, policy.getName() );
    }

   /**
    * Test warn priority.
    * @exception Exception if an error occurs
    */
    public void testWarnPriority() throws Exception
    {
        String name = "warn";
        Priority policy = Priority.parse( name );
        assertEquals( "name", name, policy.getName() );
    }

   /**
    * Test error priority.
    * @exception Exception if an error occurs
    */
    public void testErrorPriority() throws Exception
    {
        String name = "error";
        Priority policy = Priority.parse( name );
        assertEquals( "name", name, policy.getName() );
    }
    
   /**
    * Test error priority.
    * @exception Exception if an error occurs
    */
    public void testDebugPriority() throws Exception
    {
        String name = "debug";
        Priority policy = Priority.parse( name );
        assertEquals( "name", name, policy.getName() );
    }

   /**
    * Test bad policy.
    * @exception Exception if an error occurs
    */
    public void testBadPolicy() throws Exception
    {
        try
        {
            Priority policy = Priority.parse( "???" );
            fail( "Bad priority parse argument succeeded." );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }
}
