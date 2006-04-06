/* 
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.job.test;

import net.dpml.job.TimeoutException;

import junit.framework.TestCase;

/**
 * Validation of the timeout exception.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TimeoutExceptionTestCase extends TestCase
{
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testDurationValue() throws Exception
    {
        long timeout = 1;
        TimeoutException exception = new TimeoutException( timeout );
        assertEquals( "timeout", timeout, exception.getDuration() );
    }
}
