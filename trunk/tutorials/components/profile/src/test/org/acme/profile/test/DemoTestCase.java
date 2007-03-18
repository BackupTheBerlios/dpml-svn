/* 
 * Copyright 2007 Stephen J. McConnell.
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

package org.acme.profile.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.lang.Strategy;

import org.acme.profile.Demo;

/**
 * Deployment of the demo component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DemoTestCase extends TestCase
{
   /**
    * Test component deployment using the simple scenario under which
    * no context info is declared - as such we should be picking up values
    * declared in the packaged profile.
    *
    * @exception Exception if an error occurs
    */
    public void testSimpleScenario() throws Exception
    {
        URI uri = getPartURI( "simple.part" );
        Strategy strategy = Strategy.load( uri );
        Demo demo = strategy.getInstance( Demo.class );
        assertEquals( "first", "Fred", demo.getFirstName() );
        assertEquals( "first", "Flintstone", demo.getLastName() );
    }
    
   /**
    * Test component deployment using a profile containing context
    * entry value in which case we will override the default contained
    * in the packaged profile.
    *
    * @exception Exception if an error occurs
    */
    public void testOverridingScenario() throws Exception
    {
        URI uri = getPartURI( "override.part" );
        Strategy strategy = Strategy.load( uri );
        Demo demo = strategy.getInstance( Demo.class );
        assertEquals( "first", "Bruce", demo.getFirstName() );
        assertEquals( "first", "Wayne", demo.getLastName() );
    }
    
    private URI getPartURI( String name ) throws Exception
    {
        File base = new File( System.getProperty( "project.test.dir" ) );
        File file = new File( base, name );
        return file.toURI();
    }
}
