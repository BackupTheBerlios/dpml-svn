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

package net.dpml.library.impl;

import java.io.File;
import java.io.FileOutputStream;

import net.dpml.library.model.Module;
import net.dpml.library.model.Resource;
import net.dpml.library.model.Type;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ExportTestCase extends AbstractTestCase
{   
   /**
    * Test library properties.
    */
    public void testExport() throws Exception
    {
        String spec = "dpml";
        Module module = getLibrary().getModule( spec );
        ModuleDirective directive = module.export();
        ModuleBuilder builder = new ModuleBuilder();
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File export = new File( test, "export.xml" );
        builder.write( directive, new FileOutputStream( export ) );
        ModuleDirective result = builder.load( export.toURL() );
        assertEquals( "export", directive, result );
    }
    
    /*
    private void compareModuleDirective( ModuleDirective a, ModuleDirective b )
    {
        ResourceDirective[] ar = a.getResourceDirectives();
        ResourceDirective[] br = b.getResourceDirectives();
        for( int i=0; i<ar.length; i++ )
        {
            ResourceDirective r = ar[i];
            ResourceDirective rx = br[i];
            boolean equal = r.equals( rx );
            if( !equal )
            {
                compareResourceDirective( r, rx );
            }
        }
    }
    
    private void compareResourceDirective( ResourceDirective a, ResourceDirective b )
    {
        System.out.println( a.getName() + " is not equal" );
        if( a instanceof ModuleDirective )
        {
            compareModuleDirective( (ModuleDirective) a, (ModuleDirective) b );
        }
        
        if( !a.getName().equals( b.getName() ) )
        {
            System.out.println( "different names" );
        }
        if( !a.getVersion().equals( b.getVersion() ) )
        {
            System.out.println( "different versions" );
        }
        if( !a.getClassifier().equals( b.getClassifier() ) )
        {
            System.out.println( "different classifiers" );
        }
        if( a.getTypeDirectives().length != b.getTypeDirectives().length )
        {
            System.out.println( "different types length" );
        }
        if( a.getDependencyDirectives().length != b.getDependencyDirectives().length )
        {
            System.out.println( "different dependency length in " + a.getName() );
            System.out.println( "A: " + a.getDependencyDirectives().length );
            System.out.println( "B: " + b.getDependencyDirectives().length );
        }
    }
    */
}
