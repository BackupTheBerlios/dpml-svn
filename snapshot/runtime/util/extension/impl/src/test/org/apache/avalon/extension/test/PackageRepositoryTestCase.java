/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.extension.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.avalon.extension.Extension;
import org.apache.avalon.extension.manager.ExtensionManager;
import org.apache.avalon.extension.manager.OptionalPackage;
import org.apache.avalon.extension.manager.PackageManager;
import org.apache.avalon.extension.manager.impl.DefaultExtensionManager;

/**
 * TestCases for PackageRepository.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PackageRepositoryTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class PackageRepositoryTestCase
    extends TestCase
{
    private File m_baseDirectory;
    private File m_pathElement1;
    private File m_pathElement2;
    private File[] m_path;

    public PackageRepositoryTestCase( String name )
        throws IOException
    {
        super( name );

        m_baseDirectory = 
          new File( 
            getTestClassesDir(), 
            "org/apache/avalon/extension/test/" 
          ).getCanonicalFile();

        m_pathElement1 = new File( m_baseDirectory, "path1" );
        m_pathElement2 = new File( m_baseDirectory, "path2" );
        m_path = new File[]{m_pathElement1, m_pathElement2};
    }

    private File getTestClassesDir()
    {
        String path = System.getProperty( "basedir" );
        File root = new File( path );
        return new File( root, "target/test-classes" ); 
    }

    public void testGoodPath()
        throws Exception
    {
        new DefaultExtensionManager( m_path );
    }

    public void testBadPath()
        throws Exception
    {
        try
        {
            final File pathElement3 = new File( m_baseDirectory, "path3" );
            final File[] path = new File[]{m_pathElement1, m_pathElement2, pathElement3};
            new DefaultExtensionManager( path );
        }
        catch( final IllegalArgumentException iae )
        {
            return;
        }

        assertTrue( "Exceptected to fail with bad path element", false );
    }

    public void testBasicScanDependencies()
        throws Exception
    {
        final ExtensionManager repository = newPackagerepository();
        doRepositoryTest( repository );
    }

    public void testFSScanDependencies()
        throws Exception
    {
        final ExtensionManager repository = new DefaultExtensionManager( m_path );
        doRepositoryTest( repository );
    }

    private void doRepositoryTest( final ExtensionManager repository )
        throws Exception
    {
        final PackageManager manager = new PackageManager( repository );

        final Manifest manifest2 = getManifest( "manifest-2.mf" );
        final Extension extension1 = Extension.getRequired( manifest2 )[ 0 ];

        final ArrayList dependencies = new ArrayList();
        final ArrayList unsatisfied = new ArrayList();

        manager.scanDependencies( extension1, new Extension[ 0 ], dependencies, unsatisfied );

        assertEquals( "dependencies Count", 2, dependencies.size() );
        assertEquals( "unsatisfied Count", 0, unsatisfied.size() );

        final int size = dependencies.size();
        for( int i = 0; i < size; i++ )
        {
            final OptionalPackage optionalPackage = (OptionalPackage)dependencies.get( i );
            final Extension[] extensions = optionalPackage.getAvailableExtensions();
            for( int j = 0; j < extensions.length; j++ )
            {
                final String name = extensions[ j ].getExtensionName();
                if( !name.equals( "avalon.required1" ) &&
                    !name.equals( "avalon.required2" ) )
                {
                    assertTrue( "Unexpected extension: " + name, false );
                }
            }
        }
    }

    private Manifest getManifest( final String name )
        throws Exception
    {
        final InputStream inputStream = getClass().getResourceAsStream( name );
        return new Manifest( inputStream );
    }

    private ExtensionManager newPackagerepository()
        throws Exception
    {
        final TestPackageRepository repository = new TestPackageRepository();
        repository.addEntry( "manifest-1.mf" );
        repository.addEntry( "manifest-2.mf" );
        repository.addEntry( "manifest-3.mf" );
        repository.addEntry( "manifest-4.mf" );
        repository.addEntry( "manifest-5.mf" );
        repository.addEntry( "manifest-6.mf" );
        repository.addEntry( "manifest-7.mf" );
        repository.addEntry( "manifest-8.mf" );
        return repository;
    }
}

class TestPackageRepository
    extends DefaultExtensionManager
{
    TestPackageRepository()
        throws Exception
    {
        super( new File[ 0 ] );
    }

    void addEntry( final String manifestLocation )
        throws Exception
    {
        final InputStream inputStream = getClass().getResourceAsStream( manifestLocation );
        final Manifest manifest = new Manifest( inputStream );
        final File file = new File( manifestLocation );
        final Extension[] available = Extension.getAvailable( manifest );
        final Extension[] required = Extension.getRequired( manifest );

        cacheOptionalPackage( new OptionalPackage( file, available, required ) );
    }
}
