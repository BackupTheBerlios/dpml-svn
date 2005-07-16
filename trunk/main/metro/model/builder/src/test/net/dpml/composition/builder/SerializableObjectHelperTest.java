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
package net.dpml.composition.builder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import junit.framework.TestCase;
import net.dpml.composition.data.ComponentProfile;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SerializableObjectHelperTest extends TestCase
{
    private File partFile;

    public void _testComponentProfileSerialization() throws Exception
    {
        ComponentProfile profile = new ComponentProfile( "test", "acme.Test" );
        SerializableObjectHelper.write( profile, new File( "Test.type" ) );
    }

    public void testXmlSerialization() throws Exception
    {
        ComponentProfile profile = new ComponentProfile( "test", "acme.Test" );
        XStream XStream = new XStream( new DomDriver() );
        XStream.alias( "componentprofile", ComponentProfile.class );
        XStream.toXML( profile, new FileWriter( partFile ) );
        ComponentProfile result = (ComponentProfile) XStream
                .fromXML( new FileReader( partFile ) );
        assertNotNull( result );

    }
    public void setUp()
    {
        partFile = new File( System.getProperty("project.dir"), "Test.part" );
        System.out.println(partFile.getAbsolutePath());
    }

}
