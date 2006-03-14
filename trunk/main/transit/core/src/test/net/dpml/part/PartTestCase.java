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
import java.net.URI;
import java.net.URL;

import net.dpml.part.Part;
import net.dpml.part.Strategy;
import net.dpml.part.PartDirective;
import net.dpml.part.PartBuilder;
import net.dpml.part.UnresolvableHandlerException;
import net.dpml.part.PartHandlerFactory;
import net.dpml.part.PartHandler;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentType;


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
    
    PartBuilder m_builder;
    
   /**
    * Test the demo class.
    */
    public void setUp() throws Exception
    {
        m_builder = new PartBuilder( null );
    }
    
   /**
    * Test the demo class.
    */
    public void testPlugin() throws Exception
    {
        evaluateDocument( "plugin.xml" );
    }
    
   /**
    * Test the demo class.
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
        
        Part part = m_builder.loadPart( file.toURI() );
        File out = new File( test, "export-" + path );
        FileOutputStream output = new FileOutputStream( out );
        m_builder.writePart( part, output, "" );
        Part newPart = m_builder.loadPart( out.toURI() );
        assertEquals( "part", part, newPart );
    }
}
