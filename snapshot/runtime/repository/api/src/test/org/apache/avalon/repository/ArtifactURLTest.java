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
 * @version $Id: ArtifactURLTest.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ArtifactURLTest extends TestCase
{
    /**
     * Constructor for ArtifactURLTest.
     * @param name the test name
     */
    public ArtifactURLTest( String name )
    {
        super( name );
    }
    
    public void testArtifactURLGroup() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:peter/rabbit" );
        assertTrue( "peter".equals( artifact.getGroup() ) );
    }

    public void testArtifactURLCompositeGroup() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:the/peter/rabbit" );
        assertTrue( "the/peter".equals( artifact.getGroup() ) );
    }

    public void testArtifactURLName() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:peter/rabbit" );
        assertTrue( "rabbit".equals( artifact.getName() ) );
    }

    public void testArtifactURLType() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:peter/rabbit" );
        assertTrue( "jar".equals( artifact.getType() ) );
    }

    public void testArtifactURLNullVersion() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:peter/rabbit" );
        assertNull( artifact.getVersion() );
    }

    public void testArtifactRefVersionURL() throws Exception
    {
        Artifact artifact = 
          Artifact.createArtifact( "artifact:peter/rabbit#1.1" );
        assertTrue( "1.1".equals( artifact.getVersion() ) );
    }

}
