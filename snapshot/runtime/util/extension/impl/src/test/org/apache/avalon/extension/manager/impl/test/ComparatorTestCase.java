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

package org.apache.avalon.extension.manager.impl.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.avalon.extension.Extension;
import org.apache.avalon.extension.manager.ExtensionManager;
import org.apache.avalon.extension.manager.OptionalPackage;
import org.apache.avalon.extension.manager.impl.DelegatingExtensionManager;

/**
 * A basic test case for comparator.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ComparatorTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ComparatorTestCase
    extends TestCase
{
    private static final String NAME = "Extension1";
    private static final String VENDOR1 = "Vendor1";
    private static final String VENDOR2 = "Vendor2";

    public ComparatorTestCase( final String name )
    {
        super( name );
    }

    public void testAllNull()
    {
        runCompareTest( null, null, null, null, VENDOR1, VENDOR2 );
    }

    public void testSpecNonNullV1()
    {
        runCompareTest( "1.0", null, "1.1", null, VENDOR2, VENDOR1 );
    }

    public void testSpecNonNullV2()
    {
        runCompareTest( "1.1", null, "1.0", null, VENDOR1, VENDOR2 );
    }

    public void testSpecNonNullV3()
    {
        runCompareTest( "1.1", null, "1.1", null, VENDOR1, VENDOR2 );
    }

    public void testSpec1Null()
    {
        runCompareTest( null, null, "1.1", null, VENDOR2, VENDOR1 );
    }

    public void testSpec2Null()
    {
        runCompareTest( "1.1", null, null, null, VENDOR1, VENDOR2 );
    }

    public void testImplNull()
    {
        runCompareTest( "1.0", null, "1.0", null, VENDOR1, VENDOR2 );
    }

    public void testImplNonNullV1()
    {
        runCompareTest( "1.0", "1.1", "1.0", "1.0", VENDOR1, VENDOR2 );
    }

    public void testImplNonNullV2()
    {
        runCompareTest( "1.0", "1.0", "1.0", "1.1", VENDOR2, VENDOR1 );
    }

    public void testImplNonNullV3()
    {
        runCompareTest( "1.0", "1.1", "1.0", "1.1", VENDOR1, VENDOR2 );
    }

    public void testImpl1Null()
    {
        runCompareTest( "1.0", null, "1.0", "1.1", VENDOR2, VENDOR1 );
    }

    public void testImpl2Null()
    {
        runCompareTest( "1.0", "1.1", "1.0", null, VENDOR1, VENDOR2 );
    }

    private void runCompareTest( final String specVersion1,
                                 final String implVersion1,
                                 final String specVersion2,
                                 final String implVersion2,
                                 final String vendor1,
                                 final String vendor2 )
    {
        final ExtensionManager manager =
            createExtensionManager( specVersion1, implVersion1, specVersion2, implVersion2 );
        final OptionalPackage[] pkgs = getOptionalPackages( manager );

        assertEquals( "pkgs.length", 2, pkgs.length );

        final Extension extension1 = pkgs[ 0 ].getAvailableExtensions()[ 0 ];
        final Extension extension2 = pkgs[ 1 ].getAvailableExtensions()[ 0 ];
        assertEquals( "pkgs[0].vendor", vendor1, extension1.getImplementationVendor() );
        assertEquals( "pkgs[1].vendor", vendor2, extension2.getImplementationVendor() );
    }

    private OptionalPackage[] getOptionalPackages( final ExtensionManager manager )
    {
        return manager.getOptionalPackages( new Extension( NAME, null, null, null, null, null, null ) );
    }

    private ExtensionManager createExtensionManager( final String specVersion1,
                                                     final String implVersion1,
                                                     final String specVersion2,
                                                     final String implVersion2 )
    {
        final OptionalPackage optionalPackage1 =
            createPackage( VENDOR1, specVersion1, implVersion1 );
        final OptionalPackage optionalPackage2 =
            createPackage( VENDOR2, specVersion2, implVersion2 );
        final OptionalPackage[] pkgs =
            new OptionalPackage[]{optionalPackage1, optionalPackage2};
        return createExtensionManager( pkgs );
    }

    private OptionalPackage createPackage( final String vendor,
                                           final String specVersion,
                                           final String implVersion )
    {
        final Extension extension = new Extension( NAME,
                                                   specVersion, null,
                                                   implVersion, vendor, null,
                                                   null );
        final File file = new File( "." );
        final Extension[] available = new Extension[]{extension};
        final Extension[] required = new Extension[ 0 ];
        return new OptionalPackage( file, available, required );
    }

    private ExtensionManager createExtensionManager( final OptionalPackage[] packages )
    {
        final TestExtensionManager manager =
            new TestExtensionManager( packages );
        return new DelegatingExtensionManager( new ExtensionManager[]{manager} );
    }
}
