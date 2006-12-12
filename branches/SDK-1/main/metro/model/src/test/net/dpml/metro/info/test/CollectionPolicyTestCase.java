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

import net.dpml.metro.info.CollectionPolicy;

import junit.framework.TestCase;

/**
 * CategoryDescriptorTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CollectionPolicyTestCase extends TestCase
{
   /**
    * Test weak policy.
    * @exception Exception if an error occurs
    */
    public void testWeakCollectionPolicy() throws Exception
    {
        CollectionPolicy policy = CollectionPolicy.parse( "weak" );
        assertEquals( "name", "weak", policy.getName() );
    }

   /**
    * Test soft policy.
    * @exception Exception if an error occurs
    */
    public void testSoftCollectionPolicy() throws Exception
    {
        CollectionPolicy policy = CollectionPolicy.parse( "soft" );
        assertEquals( "name", "soft", policy.getName() );
    }

   /**
    * Test hard policy.
    * @exception Exception if an error occurs
    */
    public void testHardCollectionPolicy() throws Exception
    {
        CollectionPolicy policy = CollectionPolicy.parse( "hard" );
        assertEquals( "name", "hard", policy.getName() );
    }

   /**
    * Test bad policy.
    * @exception Exception if an error occurs
    */
    public void testBadCollectionPolicy() throws Exception
    {
        try
        {
            CollectionPolicy policy = CollectionPolicy.parse( "???" );
            fail( "Bad priority parse argument succeeded." );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }
}
