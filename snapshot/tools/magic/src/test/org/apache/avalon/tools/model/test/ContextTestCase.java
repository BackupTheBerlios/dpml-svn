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

package org.apache.avalon.tools.model.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.avalon.tools.model.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;


/**
 * ContextTestCase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class ContextTestCase extends TestCase
{
    private Context m_context;

    public void setUp()
    {
        Project project = new Project();
        project.setName( "test" );
        m_context = Context.getContext( project );
    }

    public ContextTestCase ( String name )
    {
        super( name );
    }

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
        Context context = Context.getContext( project );
        String key = context.getKey();
        assertTrue( "test".equals( key ) );
    }

    public void testKeyFromProperty() throws Exception
    {
        Project project = new Project();
        project.setProperty( "project.key", "xyz" );
        Context context = Context.getContext( project );
        String key = context.getKey();
        assertTrue( "xyz".equals( key ) );
    }

    public void testSrcDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getSrcDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File src = new File( dir, "src" );
        assertEquals( src, test );
    }

    public void testEtcDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getEtcDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File etc = new File( dir, "etc" );
        assertEquals( etc, test );
    }

    public void testBuildDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getBuildDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File build = new File( target, "build" );
        assertEquals( build, test );
    }

    public void testTargetDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getTargetDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        assertEquals( target, test );
    }

    public void testDeliverablesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getDeliverablesDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File deliverables = new File( target, "deliverables" );
        assertEquals( deliverables, test );
    }

    public void testClassesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getClassesDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File classes = new File( target, "classes" );
        assertEquals( classes, test );
    }

    public void testTestClassesDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getTestClassesDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File classes = new File( target, "test-classes" );
        assertEquals( classes, test );
    }

    public void testTestReportsDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getTestReportsDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File reports = new File( target, "test-reports" );
        assertEquals( reports, test );
    }

    public void testTempDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getTempDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File temp = new File( target, "temp" );
        assertEquals( temp, test );
    }

    public void testTestDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getTestDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File testDir = new File( target, "test" );
        assertEquals( testDir, test );
    }

    public void testDocsDirectory() throws Exception
    {
        Project project = new Project();
        project.setName( "test" );
        Context context = Context.getContext( project );
        File test = context.getDocsDirectory();
        File dir = new File( System.getProperty( "user.dir" ) );
        File target = new File( dir, "target" );
        File docs = new File( target, "docs" );
        assertEquals( docs, test );
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
}
