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

package org.apache.avalon.repository;


import junit.framework.TestCase;


/**
 * Create of a new Artifact test case.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ArtifactTest.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ArtifactTest extends TestCase
{
    /**
     * Constructor for ArtifactReferenceTest.
     * @param name the test name
     */
    public ArtifactTest( String name )
    {
        super( name );
    }
    

    public void testStaticOne() throws Exception
    {
        try
        {
            Artifact.createArtifact( null, null, null );
            fail( "No null pointer exception (case 1)" );
        }
        catch( Throwable e )
        {
            assertTrue( true );
        }
    }

    public void testStaticTwo() throws Exception
    {
        try
        {
            Artifact.createArtifact( null, "xxx", null );
            fail( "No null pointer exception (case 2)" );
        }
        catch( Throwable e )
        {
            assertTrue( true );
        }
    }

    public void testURLGeneralIntegrity() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "xxx", "yyy", "zzz" );
        assertNotNull( artifact );
        assertEquals( 
          "url",
          artifact.getURL( "http://www.dpml.net" ),
            "http://www.dpml.net/xxx/jars/yyy-zzz.jar" );
    }

    public void testURLFromEmptyRepository() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "xxx", "yyy", "zzz" );
        assertNotNull( artifact );
        assertEquals( 
          "url-from-empty", 
          artifact.getURL(),
          "/xxx/jars/yyy-zzz.jar" );
    }
}
