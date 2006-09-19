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

import org.acme.Demo;
import org.acme.Demo.Context;

/**
 * Deployment of the demo component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MockTestCase extends TestCase
{
    private static final String ACTIVITY = "Painting";
    private static final String OWNER = System.getProperty( "user.name" );
    private static final String TARGET = "house";
    private static final String COLOR = "red";
    private static final String MESSAGE = 
      ACTIVITY + " " + OWNER + "'s " + TARGET + " " + COLOR + ".";
      
   /**
    * Test construction of the demo instance using a mock context object.
    * @exception Exception if an error occurs
    */
    public void testComponent() throws Exception
    {
        Logger logger = Logger.getLogger( "test" );
        Context context = new MockContext();
        Demo demo = new Demo( logger, context );
        String message = demo.getMessage();
        assertEquals( "message", MESSAGE, message );
    }
    
   /**
    * The mock context object.
    */
    private static final class MockContext implements Context
    {
       /**
        * Return a string describing an activity that our object should 
        * perform. An activity is a word such as "painting" or "coloring" 
        * or any other color related activity you can think of.  The component
        * implementation will construct a phrase using this word as the operative
        * activity.
        *
        * @return the activity verb
        */
        public String getActivity()
        {
            return ACTIVITY;
        }
        
       /**
        * When constructing a phrase the implementation uses a owner to 
        * distringuish the ownership of the subject to which it is applying 
        * an activity. The value returned by this method could be a user's name
        * or an alias such as "batman".
        *
        * @return the owner's name
        */
        public String getOwner()
        {
            return OWNER;
        }
        
       /**
        * The object implementation applies an activity to an owners object.  The
        * name of the object is provided in the form of a target.  A target could
        * be a house, a bike, a car, or whatever object appeals to the manager of 
        * the object.
        *
        * @return the name of the owner's target to which the activity will
        *   be applied
        */
        public String getTarget()
        {
            return TARGET;
        }
        
       /**
        * Returns the color to be used during construction of the activity statement.
        * 
        * @return the color value
        */
        public String getColor()
        {
            return COLOR;
        }
    }
}
