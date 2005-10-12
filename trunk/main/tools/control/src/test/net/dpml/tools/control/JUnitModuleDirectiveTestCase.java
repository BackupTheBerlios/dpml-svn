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

package net.dpml.tools.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import junit.framework.TestCase;

import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.control.ModuleDirectiveBuilder;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class JUnitModuleDirectiveTestCase extends AbstractTestCase
{   
    private ModuleDirective m_module;
    
    public void setUp() throws Exception
    {
        m_module = load( "junit.xml" );
    }
    
    public void testEncoding() throws Exception
    {
        doEncodingTest( m_module, "junit-module-descritor.xml" );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_module );
    }
}
