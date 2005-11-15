/* 
 * Copyright 2004 Peter Neubauer.
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
package net.dpml.composition.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import junit.framework.TestCase;

import net.dpml.composition.tools.SerializableObjectHelper;

import net.dpml.component.data.ComponentDirective;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SerializableObjectHelperTest extends TestCase
{
    private File partFile;

    public void _testComponentDirectiveSerialization() throws Exception
    {
        ComponentDirective profile = new ComponentDirective( "test", "acme.Test" );
        SerializableObjectHelper.write( profile, new File( "Test.type" ) );
    }

    public void testXmlSerialization() throws Exception
    {
        ComponentDirective profile = new ComponentDirective( "test", "acme.Test" );
        XStream XStream = new XStream( new DomDriver() );
        XStream.alias( "componentprofile", ComponentDirective.class );
        XStream.toXML( profile, new FileWriter( partFile ) );
        ComponentDirective result = (ComponentDirective) XStream
                .fromXML( new FileReader( partFile ) );
        assertNotNull( result );

    }
    public void setUp()
    {
        partFile = new File( System.getProperty("project.test.dir"), "Test.part" );
        System.out.println(partFile.getAbsolutePath());
    }

    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
    }


}
