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

import net.dpml.util.DOM3DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test example application sources.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class RegistryXMLTestCase extends TestCase
{
   /**
    * Validate the registry.xml example.
    * @exception Exception if an unexpected test error occurs
    */
    public void testRegistryXML() throws Exception
    {
        Element element = getRootElement( "registry.xml" );

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
