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

package org.apache.avalon.tools.tasks;

import org.apache.avalon.tools.model.Context;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

/**
 * Load a goal. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PrepareTask extends SystemTask
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
      "**/*.exe,**/*.jar*,**/*.gif,**/*.jpeg,**/*.jpg";

    public void init() throws BuildException 
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty(
              SRC_FILTERED_INCLUDES_KEY, SRC_FILTERED_INCLUDES_VALUE );
            project.setNewProperty(
              ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            project.setNewProperty(
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
            log( "creating target directory" );
            mkDir( target );
        }

        final File testBase = getContext().getTestDirectory();
        if( !testBase.exists() )
        {
            log( "creating test directory" );
            mkDir( testBase );
        }

        final File src = getContext().getSrcDirectory();
        final File etc = getContext().getEtcDirectory();
        final File build = getContext().getBuildDirectory();
        final File buildEtcDir = new File( build, "etc" );

        if( src.exists() )
        {
            final String main = getSrcMain();
            final String config = getSrcConfig();
            final String test = getSrcTest();
            
            prepareMain( src, build, main, Context.SRC_MAIN );
            prepareMain( src, build, config, Context.SRC_CONFIG );
            prepareMain( src, build, test, Context.SRC_TEST );

            //
            // and any non-standard stuff
            //

            final String excludes =
              main + "/**," + config + "/**," + test + "/**";

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
            copy( etc, buildEtcDir, true, includes, excludes );
            copy( etc, buildEtcDir, false, excludes, "" );
        }

        //
        // if there is a etc/deliverable directory, then copy the 
        // content to the target/deliverables directory
        //

        final File extra = new File( buildEtcDir, "deliverables" );
        if( extra.exists() )
        {
            final File deliverables = getContext().getDeliverablesDirectory();
            copy( extra, deliverables, false, "**/*", "" );
        }
    }

    private String getSrcMain()
    {
        final String path = getProject().getProperty( Context.SRC_MAIN_KEY );
        if( null != path ) 
            return path;
        return Context.SRC_MAIN;
    }

    private String getSrcConfig()
    {
        final String path = getProject().getProperty( Context.SRC_CONFIG_KEY );
        if( null != path ) 
            return path;
        return Context.SRC_CONFIG;
    }

    private String getSrcTest()
    {
        final String path = getProject().getProperty( Context.SRC_TEST_KEY );
        if( null != path ) return path;
        return Context.SRC_TEST;
    }

    private void prepareMain( 
       final File projectSrc, final File targetMain, final String source, 
       final String path )
    {
        if( null == projectSrc ) 
            throw new NullPointerException( "projectSrc" );
        if( null == targetMain ) 
            throw new NullPointerException( "targetMain" );
        if( null == source ) 
            throw new NullPointerException( "source" );
        if( null == path ) 
            throw new NullPointerException( "path" );

        final File src = new File( projectSrc, source );
        if( src.exists() )
        {
            log( 
              "Adding content to target/main/" 
              + path + " from " + source, 
              Project.MSG_VERBOSE );

            final File dest = new File( targetMain, path );
            mkDir( dest );
            final String filters = 
              getProject().getProperty( SRC_FILTERED_INCLUDES_KEY );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/*.*", filters );
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
