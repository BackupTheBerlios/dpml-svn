/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.part;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Plugin test case.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartTestCase extends TestCase
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
        
   /**
    * Test the demo class.
    * @exception Exception if a error occurs during test execution
    */
    public void testPlugin() throws Exception
    {
        evaluateDocument( "plugin.xml" );
    }
    
   /**
    * Test the demo class.
    * @exception Exception if a error occurs during test execution
    */
    public void testResource() throws Exception
    {
        evaluateDocument( "resource.xml" );
    }
    
    private void evaluateDocument( String path ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, path );
        System.out.println( "source: " + file + " (" + file.exists() + ")" );
        
        Part part = Part.load( file.toURI() );
        File out = new File( test, "export-" + path );
        FileOutputStream output = new FileOutputStream( out );
        part.encode( output );
        Part newPart = Part.load( out.toURI() );
        assertEquals( "part", part, newPart );
    }
}
