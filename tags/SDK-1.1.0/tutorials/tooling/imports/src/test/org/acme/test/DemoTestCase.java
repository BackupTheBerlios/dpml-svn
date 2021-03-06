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

package org.acme.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.acme.SimpleClock;

/**
 * Deployment of the demo component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DemoTestCase extends TestCase
{
   /**
    * Just log the timestamp to the test output.
    * @exception Exception if an error occurs
    */
    public void testTheClock() throws Exception
    {
        SimpleClock clock = new SimpleClock();
        String timestamp = clock.getTimestamp();
        Logger logger = Logger.getLogger( "test" );
        logger.info( timestamp );
    }
}
