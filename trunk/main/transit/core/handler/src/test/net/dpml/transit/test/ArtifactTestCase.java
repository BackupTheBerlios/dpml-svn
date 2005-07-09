/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit.test;

import junit.framework.TestCase;

import net.dpml.transit.runtime.ClassicLayout;
import net.dpml.transit.runtime.Layout;
import net.dpml.transit.artifact.Artifact;

/**
 * Create of a new Artifact test case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ArtifactTestCase.java 2900 2005-06-22 19:10:15Z mcconnell@dpml.net $
 */
public class ArtifactTestCase extends TestCase
{
    /**
     * Constructor for ArtifactReferenceTest.
     * @param name the test name
     */
    public ArtifactTestCase( String name )
    {
        super( name );
    }

    public void testNullPathConstructor() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( (String) null );
            fail( "No IllegalArgumentException thrown for null uri spec: " + artifact );
        }
        catch( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }

    public void testMissingProtocol() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "jar:/group/sub-group/name#version" );
            fail( "Illegal argument exception not thrown for missing protocol: " + artifact );
        }
        catch( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }

    public void testBadGroup() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "artifact:jar://sub-group/name#version" );
            fail( "Illegal argument exception not thrown for bad group: " + artifact );
        }
        catch( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }

    public void testAnotherBadGroup() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "artifact:jar:group//name#version" );
            fail( "Illegal argument exception not thrown for bad group: " + artifact );
        }
        catch( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }

    public void testBadVersion() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "artifact:jar:group/name#version/xxx" );
            list( artifact );
            fail( "Illegal argument exception not thrown for bad version: " + artifact );
        }
        catch( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }

    public void testFullSpec() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "artifact:jar:group/sub-group/name#version");
        verify( artifact, "group/sub-group", "name", "jar", "version" );
    }

    public void testLeadingSlash() throws Exception
    {
        try
        {
            String form = "artifact:jar:/group/sub-group/name#version";
            Artifact artifact = Artifact.createArtifact( form );
            fail( "illegal format not caught: " + artifact );
        } catch( IllegalArgumentException e )
        {}
    }

    public void testMissingType() throws Exception
    {
        String form = "artifact:group/sub-group/name#version";
        try
        {
            Artifact artifact = Artifact.createArtifact( form );
            fail( "illegal format not caught: " + artifact );
        } catch( IllegalArgumentException e )
        {}
    }

    public void testNullVersion() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "artifact:jar:group/sub-group/name");
        verify( artifact, "group/sub-group", "name", "jar", null );
    }

    public void testNullVersionAndNullType() throws Exception
    {
        String form = "artifact:group/sub-group/name";
        try
        {
            Artifact artifact = Artifact.createArtifact( form );
            fail( "illegal format not caught: " + artifact );
        } catch( IllegalArgumentException e )
        {}
    }

    public void testMissingGroup() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "artifact:jar:name#version" );
            fail( "Illegal argument exception not thrown:" + artifact );
        }
        catch( Throwable e )
        {
            assertTrue( true );
        }
    }

    public void testMissingGroupWithoutType() throws Exception
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( "artifact:name#version" );
            fail( "Illegal argument exception not thrown: " + artifact );
        }
        catch( Throwable e )
        {
            assertTrue( true );
        }
    }

    public void testZeroLengthVersion() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "artifact:jar:group/sub-group/name#");
        verify( artifact, "group/sub-group", "name", "jar", null );
    }

    public void testExternalForm() throws Exception
    {
        final String spec = "artifact:jar:group/sub-group/name#version";
        Artifact artifact = Artifact.createArtifact( spec );
        assertEquals( artifact.toString(), spec );
    }

    public void testInternalReference1() throws Exception
    {
        final String spec = "artifact:jar:group/sub-group/name#version";
        final String url = spec + "!/some/resource.abc";
        Artifact artifact = Artifact.createArtifact( url );
        assertEquals( spec, artifact.toString() );
    }

    public void testInternalReference2() throws Exception
    {
        final String spec = "artifact:jar:group/sub-group/name#version";
        final String url = "artifact:jar:group/sub-group/name!/some/resource.abc#version";
        Artifact artifact = Artifact.createArtifact( url );
        assertEquals( spec, artifact.toString() );
    }

    public void testExternalFormWithNonDefaultType() throws Exception
    {
        final String spec = "artifact:block:group/sub-group/name#version";
        Artifact artifact = Artifact.createArtifact( spec );
        assertEquals( spec, spec, artifact.toString() );
    }

    public void testEquality() throws Exception
    {
        final String spec = "artifact:jar:group/sub-group/name#version";
        Artifact artifact1 = Artifact.createArtifact( spec );
        Artifact artifact2 = Artifact.createArtifact( spec );
        assertTrue( spec, artifact1.equals( artifact2 ) );
    }

    public void testInequality() throws Exception
    {
        final String spec1 = "artifact:jar:group/sub-group/name#version";
        final String spec2 = "artifact:jar:group/sub-group/name";
        Artifact artifact1 = Artifact.createArtifact( spec1 );
        Artifact artifact2 = Artifact.createArtifact( spec2 );
        assertFalse( artifact1.equals( artifact2 ) );
    }

    public void testInequalityOnType() throws Exception
    {
        final String spec1 = "artifact:jar:group/sub-group/name#version";
        final String spec2 = "artifact:block:group/sub-group/name#version";
        Artifact artifact1 = Artifact.createArtifact( spec1 );
        Artifact artifact2 = Artifact.createArtifact( spec2 );
        assertFalse( artifact1.equals( artifact2 ) );
    }

    public void testComparability1() throws Exception
    {
        final String spec1 = "artifact:jar:aaa/name";
        final String spec2 = "artifact:jar:bbb/name";
        Artifact artifact1 = Artifact.createArtifact( spec1 );
        Artifact artifact2 = Artifact.createArtifact( spec2 );

        assertTrue( artifact1.compareTo( artifact2 ) < 0 );
        assertTrue( artifact2.compareTo( artifact1 ) > 0 );
        assertTrue( artifact1.compareTo( artifact1 ) == 0 );
        assertTrue( artifact2.compareTo( artifact2 ) == 0 );
    }

    public void testComparability2() throws Exception
    {
        final String spec1 = "artifact:jar:aaa/name";
        final String spec2 = "artifact:block:bbb/name";
        Artifact artifact1 = Artifact.createArtifact( spec1 );
        Artifact artifact2 = Artifact.createArtifact( spec2 );
        Artifact artifact3 = Artifact.createArtifact( spec1 );
        Artifact artifact4 = Artifact.createArtifact( spec2 );

        assertTrue( artifact1.compareTo( artifact2 ) > 0 );
        assertTrue( artifact2.compareTo( artifact1 ) < 0 );
        assertTrue( artifact1.compareTo( artifact3 ) == 0 );
        assertTrue( artifact2.compareTo( artifact4 ) == 0 );
    }

    public void verify( Artifact artifact, String group, String name, String type, String version )
    {
        assertEquals( "group", group, artifact.getGroup() );
        assertEquals( "name", name, artifact.getName() );
        assertEquals( "version", version, artifact.getVersion() );
        assertEquals( "type", type, artifact.getType() );

        Layout resolver = new ClassicLayout();

        String base = group + "/" + type + "s";
        assertEquals( "base", base, resolver.resolveBase( artifact ) );

        if( null == version )
        {
            String path = base + "/" + name + "." + type;
            assertEquals( "path", path, resolver.resolvePath( artifact ) );
        }
        else
        {
            String path = base + "/" + name + "-" + version + "." + type;
            assertEquals( "path", path, resolver.resolvePath( artifact ) );
        }
    }

    public void list( Artifact artifact )
    {
        System.out.println( "GROUP: " + artifact.getGroup() );
        System.out.println( "NAME: " + artifact.getName() );
        System.out.println( "TYPE: " + artifact.getType() );
        System.out.println( "VERSION: " + artifact.getVersion() );
    }
}
