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

import java.io.InputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.avalon.extension.Extension;

/**
 * TestCases for Extension.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExtensionTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ExtensionTestCase
    extends TestCase
{
    private static final String MF1_NAME = "avalon.extension";
    private static final String MF1_SVERSION = "1.0.1";
    private static final String MF1_SVENDOR = "Avalon Apache";
    private static final String MF1_IVENDORID = "org.apache.avalon";
    private static final String MF1_IVENDOR = "Avalon Apache Project";
    private static final String MF1_IVERSION = "1.0.2";
    private static final String MF1_IURL = null;

    private static final String MF2_NAME = "avalon.extension";
    private static final String MF2_SVERSION = "1.0.1";
    private static final String MF2_SVENDOR = "Avalon Apache";
    private static final String MF2_IVENDORID = "org.apache.avalon";
    private static final String MF2_IVENDOR = "Avalon Apache Project";
    private static final String MF2_IVERSION = "1.0.2";
    private static final String MF2_IURL = null;

    private static final String MFR1_NAME = "avalon.required1";
    private static final String MFR1_SVERSION = "1.0";
    private static final String MFR1_SVENDOR = null;
    private static final String MFR1_IVENDORID = "org.apache.avalon";
    private static final String MFR1_IVENDOR = null;
    private static final String MFR1_IVERSION = "1.0.2";
    private static final String MFR1_IURL = "http://avalon.apache.org/extension/required1.jar";

    private static final String MF3_NAME = "avalon.required1";
    private static final String MF3_SVERSION = "1.1";
    private static final String MF3_SVENDOR = "Avalon Apache";
    private static final String MF3_IVENDORID = "org.apache.avalon";
    private static final String MF3_IVENDOR = "Avalon Apache Project";
    private static final String MF3_IVERSION = "1.0.2";
    private static final String MF3_IURL = null;

    private static final String MF4_NAME = "avalon.required1";
    private static final String MF4_SVERSION = "1.0";
    private static final String MF4_SVENDOR = "Avalon Apache";
    private static final String MF4_IVENDORID = "org.apache.avalon";
    private static final String MF4_IVENDOR = "Avalon Apache Project";
    private static final String MF4_IVERSION = "1.0.3";
    private static final String MF4_IURL = null;

    public ExtensionTestCase( String name )
    {
        super( name );
    }

    private Manifest getManifest( final String name )
        throws Exception
    {
        final InputStream inputStream = getClass().getResourceAsStream( name );
        return new Manifest( inputStream );
    }

    public void testAvailable()
            throws Exception
    {
        final Manifest manifest = getManifest( "manifest-1.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF1_NAME, available[0].getExtensionName() );
        assertEquals( "Available SpecVendor", MF1_SVENDOR, available[0].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF1_SVERSION,
                available[0].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF1_IURL, available[0].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF1_IVENDOR, available[0].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF1_IVENDORID, available[0].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF1_IVERSION,
                available[0].getImplementationVersion().toString() );
    }

    public void testAvailableQuoted()
            throws Exception
    {
        final Manifest manifest = getManifest( "manifest-quoted.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF1_NAME, available[0].getExtensionName() );
        assertEquals( "Available SpecVendor", MF1_SVENDOR, available[0].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF1_SVERSION,
                available[0].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF1_IURL, available[0].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF1_IVENDOR, available[0].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF1_IVENDORID, available[0].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF1_IVERSION,
                available[0].getImplementationVersion().toString() );
    }

    public void testRequired()
        throws Exception
    {
        final Manifest manifest = getManifest( "manifest-2.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF2_NAME, available[ 0 ].getExtensionName() );
        assertEquals( "Available SpecVendor", MF2_SVENDOR, available[ 0 ].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF2_SVERSION,
                      available[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF2_IURL, available[ 0 ].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF2_IVENDOR, available[ 0 ].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF2_IVENDORID, available[ 0 ].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF2_IVERSION,
                      available[ 0 ].getImplementationVersion().toString() );

        final Extension[] required = Extension.getRequired( manifest );
        assertEquals( "Available Count", 1, required.length );
        assertEquals( "required Name", MFR1_NAME, required[ 0 ].getExtensionName() );
        assertEquals( "required SpecVendor", MFR1_SVENDOR, required[ 0 ].getSpecificationVendor() );
        assertEquals( "required SpecVersion", MFR1_SVERSION,
                      required[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "required URL", MFR1_IURL, required[ 0 ].getImplementationURL() );
        assertEquals( "required ImpVendor", MFR1_IVENDOR, required[ 0 ].getImplementationVendor() );
        assertEquals( "required ImpVendorId", MFR1_IVENDORID, required[ 0 ].getImplementationVendorID() );
        assertEquals( "required ImpVersion", MFR1_IVERSION,
                      required[ 0 ].getImplementationVersion().toString() );
    }

    public void testManifest3()
        throws Exception
    {
        final Manifest manifest = getManifest( "manifest-3.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF3_NAME, available[ 0 ].getExtensionName() );
        assertEquals( "Available SpecVendor", MF3_SVENDOR, available[ 0 ].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF3_SVERSION,
                      available[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF3_IURL, available[ 0 ].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF3_IVENDOR, available[ 0 ].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF3_IVENDORID, available[ 0 ].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF3_IVERSION,
                      available[ 0 ].getImplementationVersion().toString() );

        final Extension[] required = Extension.getRequired( manifest );
        assertEquals( "Required Count", 1, required.length );
    }

    public void testManifest4()
        throws Exception
    {
        final Manifest manifest = getManifest( "manifest-4.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF4_NAME, available[ 0 ].getExtensionName() );
        assertEquals( "Available SpecVendor", MF4_SVENDOR, available[ 0 ].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF4_SVERSION,
                      available[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF4_IURL, available[ 0 ].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF4_IVENDOR, available[ 0 ].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF4_IVENDORID, available[ 0 ].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF4_IVERSION,
                      available[ 0 ].getImplementationVersion().toString() );

        final Extension[] required = Extension.getRequired( manifest );
        assertEquals( "Available Count", 0, required.length );
    }

    public void testCompatible()
        throws Exception
    {
        final Manifest manifest2 = getManifest( "manifest-2.mf" );
        final Manifest manifest3 = getManifest( "manifest-3.mf" );
        final Manifest manifest4 = getManifest( "manifest-4.mf" );
        final Manifest manifest5 = getManifest( "manifest-5.mf" );
        final Manifest manifest6 = getManifest( "manifest-6.mf" );
        final Manifest manifest7 = getManifest( "manifest-7.mf" );

        final Extension req1 = Extension.getRequired( manifest2 )[ 0 ];
        final Extension req2 = Extension.getRequired( manifest5 )[ 0 ];
        final Extension req3 = Extension.getRequired( manifest6 )[ 0 ];
        final Extension req4 = Extension.getRequired( manifest7 )[ 0 ];

        final Extension avail3 = Extension.getAvailable( manifest3 )[ 0 ];
        final Extension avail4 = Extension.getAvailable( manifest4 )[ 0 ];

        assertTrue( "avail3.isCompatibleWith( req1 )", avail3.isCompatibleWith( req1 ) );
        assertTrue( "avail4.isCompatibleWith( req1 )", avail4.isCompatibleWith( req1 ) );
        assertTrue( "avail3.isCompatibleWith( req2 )", avail3.isCompatibleWith( req2 ) );
        assertTrue( "avail4.isCompatibleWith( req2 )", avail4.isCompatibleWith( req2 ) );
        assertTrue( "avail3.isCompatibleWith( req3 )", avail3.isCompatibleWith( req3 ) );
        assertTrue( "!avail4.isCompatibleWith( req3 )", !avail4.isCompatibleWith( req3 ) );
        assertTrue( "avail3.isCompatibleWith( req4 )", avail3.isCompatibleWith( req4 ) );
        assertTrue( "avail4.isCompatibleWith( req4 )", avail4.isCompatibleWith( req4 ) );
    }

    public void testSpacesAfterAttributes()
        throws Exception
    {
        //Note that manifest 9 is just manifest 1 with
        //spaces added to the end of each line
        final Manifest manifest = getManifest( "manifest-9.mf" );
        final Extension[] available = Extension.getAvailable( manifest );

        assertEquals( "Available Count", 1, available.length );
        assertEquals( "Available Name", MF1_NAME, available[ 0 ].getExtensionName() );
        assertEquals( "Available SpecVendor", MF1_SVENDOR, available[ 0 ].getSpecificationVendor() );
        assertEquals( "Available SpecVersion", MF1_SVERSION,
                      available[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "Available URL", MF1_IURL, available[ 0 ].getImplementationURL() );
        assertEquals( "Available ImpVendor", MF1_IVENDOR, available[ 0 ].getImplementationVendor() );
        assertEquals( "Available ImpVendorId", MF1_IVENDORID, available[ 0 ].getImplementationVendorID() );
        assertEquals( "Available ImpVersion", MF1_IVERSION,
                      available[ 0 ].getImplementationVersion().toString() );
    }
}
