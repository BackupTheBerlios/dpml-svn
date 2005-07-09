/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.project.test;

import junit.framework.TestCase;
import net.dpml.magic.project.Context;
import org.apache.tools.ant.Project;

import java.io.File;


/**
 * ContextTestCase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ContextTestCase extends TestCase
{
    public void setUp()
    {
        Project project = new Project();
        project.setName( "test" );
    }

    public void testSignature() throws Exception
    {
        String signature = Context.getSignature();
        assertEquals( 15, signature.length() );
        assertEquals( 8, signature.indexOf( "." ) );
    }

    public void testDefaultKey() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        String key = context.getKey();
        assertTrue( "test".equals( key ) );
    }

    public void testKeyFromProperty() throws Exception
    {
        Project project = new Project();
        project.setProperty( "project.key", "xyz" );
        Context context = new Context( project );
        String key = context.getKey();
        assertTrue( "xyz".equals( key ) );
    }

    public void testNonNullIndex() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        assertNotNull( "index", context.getIndex() );
    }

    public void testSrcDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getSrcDirectory();
        File src = project.resolveFile( "src" );
        assertEquals( src, test );
    }

    public void testEtcDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getEtcDirectory();
        File etc = project.resolveFile( "etc" );
        assertEquals( etc, test );
    }

    public void testTargetDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getTargetDirectory();
        // TODO: dir is never used.
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = project.resolveFile( "target" );
        assertEquals( target, test );
    }

    public void testBuildDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getBuildDirectory();
        File build = project.resolveFile( "target/build" );
        assertEquals( build, test );
    }

    public void testDeliverablesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getDeliverablesDirectory();
        File deliverables = project.resolveFile( "target/deliverables" );
        assertEquals( deliverables, test );
    }

    public void testClassesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getClassesDirectory();
        File classes = project.resolveFile( "target/classes" );
        assertEquals( classes, test );
    }

    public void testTestClassesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getTestClassesDirectory();
        File classes = project.resolveFile( "target/test-classes" );
        assertEquals( classes, test );
    }

    public void testTestReportsDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getTestReportsDirectory();
        File reports = project.resolveFile( "target/test-reports" );
        assertEquals( reports, test );
    }

    public void testTempDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getTempDirectory();
        File temp = project.resolveFile( "target/temp" );
        assertEquals( temp, test );
    }

    public void testTestDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getTestDirectory();
        File testDir = project.resolveFile( "target/test" );
        assertEquals( testDir, test );
    }

    public void testDocsDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = new Context( project );
        File test = context.getDocsDirectory();
        File docs = project.resolveFile( "target/docs" );
        assertEquals( docs, test );
    }

    /*
    public void testCanonicalFile() throws Exception
    {
        File abc = new File( "abc" );
        File xyz = new File( abc, "xyz" );
        File canonical = Context.getFile( new File( "abc" ), "xyz" );
        String path = canonical.toString();
        String alt = xyz.getCanonicalPath();
        assertEquals( path, alt );
    }

    public void testCanonicalFileWithCreation() throws Exception
    {
        File test = new File( System.getProperty( "project.dir" ));
        File file = Context.getFile( test, "junk/abc", true );
        assertTrue( file.getParentFile().exists() );
    }

    public void testPathReservation() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File dir = context.setBuildPath( "key", "gizmo" );
        File reserved = context.getBuildPath( "key" );
        assertEquals( dir, reserved );
    }

    public void testImplicitPathReservation() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File dir = context.setBuildPath( "gizmo" );
        File reserved = context.getBuildPath( "gizmo" );
        assertEquals( dir, reserved );
    }

    public void testDuplicatePathReservation() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File dir = context.setBuildPath( "key", "gizmo" );
        try
        {
            context.setBuildPath( "key", "widget" );
            fail( "build path reservation failed" );
        }
        catch( BuildException be )
        {
            // expected
        }
    }
    */

}
