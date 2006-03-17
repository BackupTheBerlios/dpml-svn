/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.station.info;

import java.io.File;

import junit.framework.TestCase;

import net.dpml.part.DOM3DocumentBuilder;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test example application sources.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ApplicationXMLTestCase extends TestCase
{
    public void testExample1() throws Exception
    {
        Element element = getRootElement( "application.xml" );
        String tag = element.getTagName();
        assertEquals( "tag", "application", tag );
        String title = ElementHelper.getAttribute( element, "title" );
        assertEquals( "title", "Test Application", title );
        String policy = ElementHelper.getAttribute( element, "policy" );
        assertEquals( "policy", "manual", policy );

        Element jvm = ElementHelper.getChild( element, "jvm" );
        assertNotNull( "jvm", jvm );
        Element startup = ElementHelper.getChild( jvm, "startup" );
        assertNotNull( "startup", startup );
        Element shutdown = ElementHelper.getChild( jvm, "shutdown" );
        assertNotNull( "shutdown", shutdown );
        String startupValue = ElementHelper.getValue( startup );
        assertEquals( "startup value", "120", startupValue );
        String shutdownValue = ElementHelper.getValue( shutdown );
        assertEquals( "shutdown value", "60", shutdownValue );
        Element properties = ElementHelper.getChild( jvm, "properties" );
        assertNotNull( "properties", properties );
        Element[] children = ElementHelper.getChildren( properties, "property" );
        assertNotNull( "properties array", children );
        assertEquals( "properties count", 1, children.length );
        Element property = children[0];
        String name = ElementHelper.getAttribute( property, "name" );
        assertEquals( "name", "foo", name );
        String value = ElementHelper.getAttribute( property, "value" );
        assertEquals( "value", "bar", value );
        
        Element codebase = ElementHelper.getChild( element, "codebase" );
        assertNotNull( "codebase", codebase );
        String uri = ElementHelper.getAttribute( codebase, "uri" );
        assertEquals( "uri", "link:part:acme/widget", uri );
        Element[] params = ElementHelper.getChildren( codebase, "param" );
        assertNotNull( "params array", params );
        assertEquals( "params count", 1, params.length );
        Element param = params[0];
        String classname = ElementHelper.getAttribute( param, "class" );
        assertEquals( "classname", "java.awt.Color", classname );
        String method = ElementHelper.getAttribute( param, "method" );
        assertEquals( "method", "RED", method );
        
    }
    
    private Element getRootElement( String filename ) throws Exception
    {
        Document doc = getDocument( filename );
        return doc.getDocumentElement();
    }
    
    private Document getDocument( String filename ) throws Exception
    {
        DOM3DocumentBuilder builder = new DOM3DocumentBuilder();
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, filename );
        return builder.parse( file.toURI() );
    }
    
}
