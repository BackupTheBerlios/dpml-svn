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

import org.apache.avalon.extension.Specification;

/**
 * TestCases for Specification.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SpecificationTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SpecificationTestCase
    extends TestCase
{
    private static final String MF1_STITLE = "org.realityforge.dve";
    private static final String MF1_SVERSION = "1.0.2";
    private static final String MF1_SVENDOR = "Peter Donald";
    private static final String MF1_ITITLE = "DVE vi OS3P";
    private static final String MF1_IVENDOR = "Peter Donald";
    private static final String MF1_IVERSION = "1.0.2Alpha";

    private static final String MF2_STITLE = "org.apache.phoenix";
    private static final String MF2_SVERSION = "1.0.2";
    private static final String MF2_SVENDOR = "Apache";
    private static final String MF2_ITITLE = "Apache Phoenix";
    private static final String MF2_IVENDOR = "Apache";
    private static final String MF2_IVERSION = "1.0.2";

    public SpecificationTestCase( String name )
    {
        super( name );
    }

    private Manifest getManifest( final String name )
        throws Exception
    {
        final InputStream inputStream = getClass().getResourceAsStream( name );
        return new Manifest( inputStream );
    }

    private Specification[] getPackageSpecifcations( final String name )
        throws Exception
    {
        final Manifest manifest = getManifest( name );
        return Specification.getSpecifications( manifest );
    }

    private void checkMissing( final int manifestID, final String attribute )
    {
        try
        {
            getPackageSpecifcations( "specification-" + manifestID + ".mf" );
        }
        catch( final Throwable t )
        {
            return;
        }
        fail( "Missing " + attribute + " parsed" );
    }

    public void testSpecifications()
        throws Exception
    {
        final Specification[] specifications = getPackageSpecifcations( "specification-1.mf" );

        assertEquals( "Count", 1, specifications.length );
        assertEquals( "Name", MF1_STITLE, specifications[ 0 ].getSpecificationTitle() );
        assertEquals( "SpecVendor", MF1_SVENDOR, specifications[ 0 ].getSpecificationVendor() );
        assertEquals( "SpecVersion", MF1_SVERSION,
                      specifications[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "ImpVendor", MF1_IVENDOR, specifications[ 0 ].getImplementationVendor() );
        assertEquals( "ImpTitle", MF1_ITITLE, specifications[ 0 ].getImplementationTitle() );
        assertEquals( "ImpVersion", MF1_IVERSION,
                      specifications[ 0 ].getImplementationVersion().toString() );
    }

    public void testSpaceAtEOL()
        throws Exception
    {
        //Note that manifest 7 is just manifest 1 with
        //spaces added to the end of each line
        final Specification[] specifications = getPackageSpecifcations( "specification-7.mf" );

        assertEquals( "Count", 1, specifications.length );
        assertEquals( "Name", MF1_STITLE, specifications[ 0 ].getSpecificationTitle() );
        assertEquals( "SpecVendor", MF1_SVENDOR, specifications[ 0 ].getSpecificationVendor() );
        assertEquals( "SpecVersion", MF1_SVERSION,
                      specifications[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "ImpVendor", MF1_IVENDOR, specifications[ 0 ].getImplementationVendor() );
        assertEquals( "ImpTitle", MF1_ITITLE, specifications[ 0 ].getImplementationTitle() );
        assertEquals( "ImpVersion", MF1_IVERSION,
                      specifications[ 0 ].getImplementationVersion().toString() );
    }

    public void testMultiSection()
        throws Exception
    {
        final Specification[] specifications = getPackageSpecifcations( "specification-8.mf" );

        assertEquals( "Count", 1, specifications.length );
        assertEquals( "Name", MF1_STITLE, specifications[ 0 ].getSpecificationTitle() );
        assertEquals( "SpecVendor", MF1_SVENDOR, specifications[ 0 ].getSpecificationVendor() );
        assertEquals( "SpecVersion", MF1_SVERSION,
                      specifications[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "ImpVendor", MF1_IVENDOR, specifications[ 0 ].getImplementationVendor() );
        assertEquals( "ImpTitle", MF1_ITITLE, specifications[ 0 ].getImplementationTitle() );
        assertEquals( "ImpVersion", MF1_IVERSION,
                      specifications[ 0 ].getImplementationVersion().toString() );
    }

    public void testMultiSectionMultiSpec()
        throws Exception
    {
        final Specification[] specifications = getPackageSpecifcations( "specification-9.mf" );

        assertEquals( "Count", 2, specifications.length );
        assertEquals( "Name", MF1_STITLE, specifications[ 0 ].getSpecificationTitle() );
        assertEquals( "SpecVendor", MF1_SVENDOR, specifications[ 0 ].getSpecificationVendor() );
        assertEquals( "SpecVersion", MF1_SVERSION,
                      specifications[ 0 ].getSpecificationVersion().toString() );
        assertEquals( "ImpVendor", MF1_IVENDOR, specifications[ 0 ].getImplementationVendor() );
        assertEquals( "ImpTitle", MF1_ITITLE, specifications[ 0 ].getImplementationTitle() );
        assertEquals( "ImpVersion", MF1_IVERSION,
                      specifications[ 0 ].getImplementationVersion().toString() );

        assertEquals( "Name", MF2_STITLE, specifications[ 1 ].getSpecificationTitle() );
        assertEquals( "SpecVendor", MF2_SVENDOR, specifications[ 1 ].getSpecificationVendor() );
        assertEquals( "SpecVersion", MF2_SVERSION,
                      specifications[ 1 ].getSpecificationVersion().toString() );
        assertEquals( "ImpVendor", MF2_IVENDOR, specifications[ 1 ].getImplementationVendor() );
        assertEquals( "ImpTitle", MF2_ITITLE, specifications[ 1 ].getImplementationTitle() );
        assertEquals( "ImpVersion", MF2_IVERSION,
                      specifications[ 1 ].getImplementationVersion().toString() );
    }

    public void testMissingSPecVersion()
        throws Exception
    {
        checkMissing( 2, "SpecVersion" );
    }

    public void testMissingSpecVendor()
        throws Exception
    {
        checkMissing( 3, "SpecVendor" );
    }

    public void testMissingImplTitle()
        throws Exception
    {
        checkMissing( 4, "ImplTitle" );
    }

    public void testMissingImplVendor()
        throws Exception
    {
        checkMissing( 5, "ImplVendor" );
    }

    public void testMissingImplVersion()
        throws Exception
    {
        checkMissing( 6, "ImplVersion" );
    }

    public void testCompatible()
        throws Exception
    {
        final Specification req1 =
            new Specification( MF1_STITLE, MF1_SVERSION, MF1_SVENDOR,
                               MF1_ITITLE, MF1_IVERSION, MF1_IVENDOR );
        final Specification req2 =
            new Specification( MF1_STITLE, MF1_SVERSION, MF1_SVENDOR,
                               null, null, null );
        final Specification req3 =
            new Specification( MF1_STITLE, "1.0.1", MF1_SVENDOR,
                               null, null, null );
        final Specification req4 =
            new Specification( MF1_STITLE, MF1_SVERSION, null,
                               null, null, null );
        final Specification req5 =
            new Specification( "another title", MF1_SVERSION, MF1_SVENDOR,
                               MF1_ITITLE, MF1_IVERSION, MF1_IVENDOR );

        final Specification avail1 =
            new Specification( MF1_STITLE, MF1_SVERSION, MF1_SVENDOR,
                               MF1_ITITLE, MF1_IVERSION, MF1_IVENDOR );
        final Specification avail2 =
            new Specification( MF1_STITLE, MF1_SVERSION, MF1_SVENDOR,
                               MF1_ITITLE, "another version", MF1_IVENDOR );
        final Specification avail3 =
            new Specification( MF1_STITLE, MF1_SVERSION, MF1_SVENDOR,
                               MF1_ITITLE, MF1_IVERSION, "another vendor" );

        assertTrue( "avail1.isCompatibleWith( req1 )", avail1.isCompatibleWith( req1 ) );
        assertTrue( "avail1.isCompatibleWith( req2 )", avail1.isCompatibleWith( req2 ) );
        assertTrue( "avail1.isCompatibleWith( req3 )", avail1.isCompatibleWith( req3 ) );
        assertTrue( "avail1.isCompatibleWith( req4 )", avail1.isCompatibleWith( req4 ) );
        assertTrue( "!avail1.isCompatibleWith( req5 )", !avail1.isCompatibleWith( req5 ) );

        assertTrue( "!avail2.isCompatibleWith( req1 )", !avail2.isCompatibleWith( req1 ) );
        assertTrue( "avail2.isCompatibleWith( req2 )", avail2.isCompatibleWith( req2 ) );
        assertTrue( "avail2.isCompatibleWith( req3 )", avail2.isCompatibleWith( req3 ) );
        assertTrue( "avail2.isCompatibleWith( req4 )", avail2.isCompatibleWith( req4 ) );
        assertTrue( "!avail2.isCompatibleWith( req5 )", !avail2.isCompatibleWith( req5 ) );

        assertTrue( "!avail3.isCompatibleWith( req1 )", !avail3.isCompatibleWith( req1 ) );
        assertTrue( "avail3.isCompatibleWith( req2 )", avail3.isCompatibleWith( req2 ) );
        assertTrue( "avail3.isCompatibleWith( req3 )", avail3.isCompatibleWith( req3 ) );
        assertTrue( "avail3.isCompatibleWith( req4 )", avail3.isCompatibleWith( req4 ) );
        assertTrue( "!avail3.isCompatibleWith( req5 )", !avail3.isCompatibleWith( req5 ) );
    }
}
