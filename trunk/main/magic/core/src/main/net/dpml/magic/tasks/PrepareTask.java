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

package net.dpml.magic.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PrepareTask extends ProjectTask
{
    private static final String SRC_FILTERED_INCLUDES_KEY =
      "project.prepare.src.filtered.includes";
    private static final String SRC_FILTERED_INCLUDES_VALUE =
      "**/*.java,**/*.x*,**/*.properties";

    private static final String ETC_FILTERED_INCLUDES_KEY =
      "project.prepare.etc.filtered.includes";
    private static final String ETC_FILTERED_INCLUDES_VALUE =
      "**/*";

    private static final String ETC_FILTERED_EXCLUDES_KEY =
      "project.prepare.etc.filtered.excludes";
    private static final String ETC_FILTERED_EXCLUDES_VALUE =
      "**/*.exe,**/*.jar*,**/*.dll,**/*.gif,**/*.jpeg,**/*.jpg,**/*.ico";

    public void init() throws BuildException
    {

        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setProperty(
              SRC_FILTERED_INCLUDES_KEY, SRC_FILTERED_INCLUDES_VALUE );
            project.setProperty(
              ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            project.setProperty(
              ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
        }
    }

    public void execute() throws BuildException
    {
        final Project project = getProject();

        //
        // setup the file system
        //

        final File target = getContext().getTargetDirectory();
        if( !target.exists() )
        {
            log( "creating target directory", Project.MSG_VERBOSE );
            mkDir( target );
        }

        final File testBase = getContext().getTestDirectory();
        if( !testBase.exists() )
        {
            log( "creating test directory", Project.MSG_VERBOSE );
            mkDir( testBase );
        }

        final File src = getContext().getSrcDirectory();
        final File etc = getContext().getEtcDirectory();
        final File build = getContext().getBuildDirectory();

        if( src.exists() )
        {
            prepareSrcMain();
            prepareSrcTest();

            //
            // and any non-standard stuff
            //

            final String excludes =
              getContext().getSrcMainDirectory().toString()
              + "/**,"
              + getContext().getSrcTestDirectory().toString()
              + "/**";

            final String filters = project.getProperty( SRC_FILTERED_INCLUDES_KEY );
            copy( src, build, true, filters, excludes );
            if( filters.length() > 0 )
            {
                copy( src, build, false, "**/*.*", excludes + "," + filters );
            }
            else
            {
                copy( src, build, false, "**/*.*", excludes );
            }
        }

        if( etc.exists() )
        {
            final String includes = project.getProperty( ETC_FILTERED_INCLUDES_KEY );
            final String excludes = project.getProperty( ETC_FILTERED_EXCLUDES_KEY );

            //
            // copy ${etc}/test content to ${target}/build/etc/test
            //

            final File buildEtcDir = new File( build, "etc" );
            final File etcTest = new File( etc, "test" );
            if( etcTest.exists() )
            {
                final File buildEtcTestDir = new File( buildEtcDir, "test" );
                copy( etcTest, buildEtcTestDir, true, includes, excludes );
                copy( etcTest, buildEtcTestDir, false, excludes, "" );
            }

            //
            // copy ${etc}/main content to ${target}/build/main
            //

            final File buildMainDir = new File( build, "main" );
            final File etcMain = new File( etc, "main" );
            if( etcMain.exists() )
            {
                copy( etcMain, buildMainDir, true, includes, excludes );
                copy( etcMain, buildMainDir, false, excludes, "" );
            }

            //
            // copy ${etc}/deliverables directory to ${target}/deliverables
            //

            final File etcDeliverables = new File( etc, "deliverables" );
            if( etcDeliverables.exists() )
            {
                final File deliverables = getContext().getDeliverablesDirectory();
                copy( etcDeliverables, deliverables, true, includes, excludes );
                copy( etcDeliverables, deliverables, false, excludes, "" );
            }

            //
            //  ${etc}/* directories (excluding test, main and deliverables)
            // directly to the target directory
            //

            final String standard = "main/**,test/**,deliverables/**,";
            copy( etc, target, true, includes, standard + excludes );
            copy( etc, target, false, excludes, standard );
        }
    }

    private void prepareSrcMain()
    {
        File main = getContext().getSrcMainDirectory();
        File build = getContext().getBuildDirectory();
        File dest = new File( build, "main" );

        if( main.exists() )
        {
            log(
              "Adding content to target/build/main/"
              + " from " + main,
              Project.MSG_VERBOSE );

            mkDir( dest );
            final String filters =
              getProject().getProperty( SRC_FILTERED_INCLUDES_KEY );
            copy( main, dest, true, filters, "" );
            copy( main, dest, false, "**/*.*", filters );
        }
    }

    private void prepareSrcTest()
    {
        File test = getContext().getSrcTestDirectory();
        File build = getContext().getBuildDirectory();
        File dest = new File( build, "test" );

        if( test.exists() )
        {
            log(
              "Adding content to target/build/test/"
              + " from " + test,
              Project.MSG_VERBOSE );

            mkDir( dest );
            final String filters =
              getProject().getProperty( SRC_FILTERED_INCLUDES_KEY );
            copy( test, dest, true, filters, "" );
            copy( test, dest, false, "**/*.*", filters );
        }
    }

    private void copy(
       final File src, final File destination, final boolean filtering, final String includes, final String excludes )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setTodir( destination );
        copy.setFiltering( filtering );
        copy.setOverwrite( false );
        copy.setPreserveLastModified( true );
        final FileSet fileset = new FileSet();
        fileset.setDir( src );
        fileset.setIncludes( includes );
        fileset.setExcludes( excludes );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }
}
