/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.tasks;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Copy;

import net.dpml.tools.ant.Definition;

/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PrepareTask extends GenericTask
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
    
    private boolean m_init = false;
    
    public PrepareTask()
    {
        super();
    }
    
   /**
    * Task initiaization during which filter include valies are established.
    * @exception BuildException if an error occurs
    */
    public void init() throws BuildException
    {
        if( !m_init )
        {
            final Project project = getProject();
            project.setProperty(
              SRC_FILTERED_INCLUDES_KEY, SRC_FILTERED_INCLUDES_VALUE );
            project.setProperty(
              ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            project.setProperty(
              ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            m_init = true;
        }
    }
    
   /**
    * Replicates the content of the src and etc directory to the target directory applying
    * a set of fixed rules - see prepare task documentation for details.
    */
    public void execute()
    {
        final Project project = getProject();
        Definition definition = getDefinition();
        
        //
        // setup the file system
        //
        
        String filters = definition.getProperty( SRC_FILTERED_INCLUDES_KEY, SRC_FILTERED_INCLUDES_VALUE );
        mkDir( definition.getTargetDirectory() );
        if( definition.getSrcMainDirectory().exists() )
        {
            File src = definition.getSrcMainDirectory();
            File dest = definition.getTargetDirectory( "build/main" );
            mkDir( dest );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/.*", filters );
        }
        if( definition.getSrcTestDirectory().exists() )
        {
            File src = definition.getSrcTestDirectory();
            File dest = definition.getTargetDirectory( "build/test" );
            mkDir( dest );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/*.*", filters );
            File test = definition.getTargetDirectory( "test" );
            mkDir( test );
        }
        if( definition.getEtcDirectory().exists() )
        {
            final String includes = 
              definition.getProperty( ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            final String excludes = 
              definition.getProperty( ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            
            //
            // copy ${etc}/test content to ${target}/build/test
            //
            
            File etc = definition.getEtcDirectory();
            final File etcTest = new File( etc, "test" );
            if( etcTest.exists() )
            {
                final File test = definition.getTargetDirectory( "test" );
                copy( etcTest, test, true, includes, excludes );
                copy( etcTest, test, false, excludes, "" );
            }
            
            //
            // copy ${etc}/main content to ${target}/build/main
            //
            
            final File etcMain = new File( etc, "main" );
            if( etcMain.exists() )
            {
                final File buildMainDir = definition.getTargetDirectory( "build/main" );
                copy( etcMain, buildMainDir, true, includes, excludes );
                copy( etcMain, buildMainDir, false, excludes, "" );
            }
            
            //
            //  ${etc}/* directories (excluding test, main and deliverables)
            // directly to the target directory
            //

            File target = definition.getTargetDirectory();
            final String standard = "main/**,test/**,";
            copy( etc, target, true, includes, standard + excludes );
            copy( etc, target, false, excludes, standard );
        }
    }
}
