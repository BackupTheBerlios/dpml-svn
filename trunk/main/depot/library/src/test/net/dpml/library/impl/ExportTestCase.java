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
import java.util.Arrays;

import net.dpml.library.Module;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.TypeDirective;

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
    * @exception Exception if an error occurs during test execution
    */
    public void testExport() throws Exception
    {
        String spec = "dpml";
        Module module = getLibrary().getModule( spec );
        ModuleDirective directive = module.export();
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File export = new File( test, "export.xml" );
        LibraryDecoder decoder = new LibraryDecoder();
        LibraryEncoder encoder = new LibraryEncoder();
        encoder.export( directive, new FileOutputStream( export ) );
        ResourceDirective result = decoder.buildResource( export.toURI() );
        compareResourceDirective( directive, result );
        assertEquals( "export", directive, result );
    }
    
    private void compareResourceDirective( ResourceDirective a, ResourceDirective b )
    {
        if( equals( a, b ) )
        {
            return;
        }
        
        System.out.println( "exported resource: " + a.getName() + " is not equals to constructed resource." );
        
        if( !a.getName().equals( b.getName() ) )
        {
            System.out.println( "# different names" );
        }
        if( !a.getVersion().equals( b.getVersion() ) )
        {
            System.out.println( "# different versions" );
        }
        if( !equals( a.getBasedir(), b.getBasedir() ) )
        {
            System.out.println( "# different basedirs" );
        }
        if( !Arrays.equals( a.getTypeDirectives(), b.getTypeDirectives() ) )
        {
            System.out.println( "# different types" );
            compareTypes( a.getTypeDirectives(), b.getTypeDirectives() );
        }
        if( !Arrays.equals( a.getDependencyDirectives(), b.getDependencyDirectives() ) )
        {
            System.out.println( "# different dependencies" );
        }
        if( !a.getClassifier().equals( b.getClassifier() ) )
        {
            System.out.println( "# different classifiers" );
        }
        if( a instanceof ModuleDirective )
        {
            compareModuleDirective( (ModuleDirective) a, (ModuleDirective) b );
        }
    }

    private void compareModuleDirective( ModuleDirective a, ModuleDirective b )
    {
        ResourceDirective[] ar = a.getResourceDirectives();
        ResourceDirective[] br = b.getResourceDirectives();
        if( ar.length != br.length )
        {
            System.out.println( "# different subsidiary resource count" );
        }
        else
        {
            for( int i=0; i<ar.length; i++ )
            {
                ResourceDirective r = ar[i];
                ResourceDirective rx = br[i];
                if( !r.equals( rx ) )
                {
                    compareResourceDirective( r, rx );
                }
            }
        }
    }
    
    private boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }
    
    private void compareTypes( TypeDirective[] a, TypeDirective[] b )
    {
        if( a.length != b.length )
        {
            System.out.println( "# different type count" );
        }
        else
        {
            for( int i=0; i<a.length; i++ )
            {
                compareType( a[i], b[i] );
            }
        }
    }
    
    private void compareType( TypeDirective a, TypeDirective b )
    {
        if( !a.equals( b ) )
        {
            if( !a.getID().equals( b.getID() ) )
            {
                System.out.println( "# different type ids: " + a.getID() + ", " + b.getID() );
            }
            else
            {
                System.out.println( "# different types for id: " + a.getID() );
                System.out.println( "# A: " + a );
                System.out.println( "# B: " + b );
            }
        }
    }
}
