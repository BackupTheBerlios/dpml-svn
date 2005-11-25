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

package net.dpml.tools.info;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProcessorDescriptorTestCase extends AbstractTestCase
{
    static ProcessorDescriptor[] PROCESSORS = new ProcessorDescriptor[3];
    static
    {
        PROCESSORS[0] = new ProcessorDescriptor( "jar" );
        PROCESSORS[1] = new ProcessorDescriptor( "acme", "actifact:plugin:acme/whatever", new String[0] );
        PROCESSORS[2] = new ProcessorDescriptor( "widget", null, new String[]{ "acme" }, PROPERTIES );
    }
    
    public void testNullName()
    {
        try
        {
            ProcessorDescriptor process = new ProcessorDescriptor( null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullDepends() throws Exception
    {
        try
        {
            ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:whatever", null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testProcessName()
    {
        ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:xxx", new String[0], PROPERTIES );
        assertEquals( "name", "test", process.getName() );
    }
    
    public void testProcessURN()
    {
        ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:xxx", new String[0], PROPERTIES );
        assertEquals( "urn", "plugin:xxx", process.getURN() );
    }
    
    public void testProcessDependencies()
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:xxx", deps, PROPERTIES );
        assertEquals( "deps", deps, process.getDependencies() );
    }
    
    public void testSerialization() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:xxx", deps, PROPERTIES );
        doSerializationTest( process );
    }

    public void testXMLEncoding() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ProcessorDescriptor process = new ProcessorDescriptor( "test", "plugin:xxx", deps, PROPERTIES );
        doEncodingTest( process, "process-descriptor-encoded.xml" );
    }
}
