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

package net.dpml.lang.process;

import java.io.File;
import java.net.URI;

import net.dpml.util.DOM3DocumentBuilder;

import junit.framework.TestCase;

/**
 * The Process XSD class validates the prodicess XSD schema.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ProcessXsdTestCase extends TestCase
{

    public void setUp() throws Exception
    {
        File testdir = new File( System.getProperty( "project.test.dir" ) );
        File doc = new File( testdir, "sample.xml" );
        URI uri = doc.toURI();
        
        File basedir = new File( System.getProperty( "project.basedir" ) );
        File target = new File( basedir, "target" );
        File deliverables = new File( target, "deliverables" );
        File xsds = new File( deliverables, "xsds" );
        String version = System.getProperty( "project.version" ); // <-- ISSUE
        File xsd = new File( xsds, "dpml-process-" + version + ".xsd" );
        System.setProperty( "@PROJECT-XSD-URI@", xsd.toURI().toString() );
        
        DOM3DocumentBuilder builder = new DOM3DocumentBuilder();
        builder.parse( uri );
    }
    
    public void testProcessXsd() throws Exception
    {
        
    }
}
