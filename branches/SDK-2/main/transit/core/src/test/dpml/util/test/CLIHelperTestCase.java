/*
 * Copyright 2005 Stephen McConnell
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

package dpml.util.test;

import dpml.util.CLIHelper;

import junit.framework.TestCase;

/**
 * Test utility functions in the CLIHelper class.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Management Laboratory</a>
 */
public class CLIHelperTestCase extends TestCase
{
   /**
    * Test the conslidation of a single argument value whereby 
    * a supplied argument is removed from an args sequence.
    * @exception Exception if the test fails
    */
    public void testConsolidation() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        String[] result = CLIHelper.consolidate( args, "aaa" );
        assertEquals( "first", result[0], "bbb" );
        assertEquals( "second", result[1], "ccc" );
        assertEquals( "length", result.length, 2 );
    }

   /**
    * Test the conslidation of a single argument and parameter.
    * The int parameter dictates the number of additional arguments to 
    * remove from the array following the parameter value.
    * @exception Exception if the test fails
    */
    public void testConsolidationWithOffset() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        String[] result = CLIHelper.consolidate( args, "aaa", 1 );
        assertEquals( "first", result[0], "ccc" );
        assertEquals( "length", result.length, 1 );
    }

   /**
    * Test the conslidation of a single argument and two parameters.
    * @exception Exception if the test fails
    */
    public void testConsolidationWithNoRemainder() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        String[] result = CLIHelper.consolidate( args, "aaa", 2 );
        assertEquals( "length", result.length, 0 );
    }

   /**
    * Test the conslidation of a single that is not the inital argument and one parameters.
    * @exception Exception if the test fails
    */
    public void testConsolidationInterim() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        String[] result = CLIHelper.consolidate( args, "bbb", 1 );
        assertEquals( "length", result.length, 1 );
        assertEquals( "value", result[0], "aaa" );
    }

   /**
    * Test the conslidation of a single that is not the inital argument and one parameters.
    * @exception Exception if the test fails
    */
    public void testIsOptionPresent() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        assertTrue( "option aaa", CLIHelper.isOptionPresent( args, "aaa" ) );
        assertTrue( "option bbb", CLIHelper.isOptionPresent( args, "bbb" ) );
        assertTrue( "option ccc", CLIHelper.isOptionPresent( args, "ccc" ) );
    }

   /**
    * Test the conslidation of a single that is not the inital argument and one parameters.
    * @exception Exception if the test fails
    */
    public void testGetOption() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb", "ccc"};
        assertEquals( "get option", CLIHelper.getOption( args, "aaa" ), "bbb" );
    }

}
