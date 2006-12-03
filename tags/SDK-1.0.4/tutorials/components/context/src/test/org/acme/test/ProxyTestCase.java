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
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import net.dpml.util.ContextInvocationHandler;

import org.acme.Demo;
import org.acme.Demo.Context;

/**
 * Deployment of the demo component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProxyTestCase extends TestCase
{
    private static final String ACTIVITY = "Painting";
    private static final String OWNER = System.getProperty( "user.name" );
    private static final String TARGET = "GTV";
    private static final String COLOR = "white";
    
    private static final String MESSAGE = 
      ACTIVITY + " " + OWNER + "'s " + TARGET + " " + COLOR + ".";
      
   /**
    * Test construction of the demo instance using a proxy context object.
    * @exception Exception if an error occurs
    */
    public void testComponent() throws Exception
    {
        Logger logger = Logger.getLogger( "test" );
        Class clazz = Demo.Context.class;
        Map map = buildContextMap();
        Context context = 
          (Context) ContextInvocationHandler.getProxiedInstance( clazz, map );
        Demo demo = new Demo( logger, context );
        String message = demo.getMessage();
        assertEquals( "message", MESSAGE, message );
    }
    
    private Map buildContextMap()
    {
        Map map = new Hashtable();
        map.put( "activity", ACTIVITY );
        map.put( "owner", OWNER );
        map.put( "target", TARGET );
        map.put( "color", COLOR );
        return map;
    }
}
