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

package net.dpml.library.info;

import java.io.File;
import java.io.FileOutputStream;

import net.dpml.library.impl.ModuleBuilder;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class BuilderTestCase extends AbstractTestCase
{
    public void testReadWrite() throws Exception
    {
        ModuleBuilder builder = new ModuleBuilder();
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File file = new File( test, "dpml/module.xml" );
        ModuleDirective module = builder.load( file.toURL() );
        
        File destination = new File( test, "externalization.xml" );
        builder.write( module, new FileOutputStream( destination ) );
    }
}
