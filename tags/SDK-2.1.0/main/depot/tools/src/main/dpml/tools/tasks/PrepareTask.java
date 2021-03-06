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

package dpml.tools.tasks;

import dpml.tools.Context;
import dpml.tools.BuilderError;

import java.io.File;
import java.io.IOException;

import dpml.library.Resource;

import org.apache.tools.ant.Project;


/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PrepareTask extends GenericTask
{
    private static final String SRC_FILTERED_INCLUDES_KEY =
      "project.prepare.src.filtered.includes";
    private static final String SRC_FILTERED_INCLUDES_VALUE =
      "**/*.java,**/*.x*,**/*.properties,**/*.html";

    private static final String ETC_FILTERED_INCLUDES_KEY =
      "project.prepare.etc.filtered.includes";
    private static final String ETC_FILTERED_INCLUDES_VALUE =
      "**/*";
    
    private static final String ETC_FILTERED_EXCLUDES_KEY =
      "project.prepare.etc.filtered.excludes";
    private static final String ETC_FILTERED_EXCLUDES_VALUE =
      "**/*.exe,**/*.jar*,**/*.dll,**/*.gif,**/*.jpeg,**/*.jpg,**/*.ico,**/*.png,**/*.keystore";
    
    private boolean m_init = false;
    
   /**
    * Task initiaization during which filter values are established.
    */
    public void init()
    {
        if( !m_init )
        {
            log( "Prepare initialization for: " 
              + getResource(), Project.MSG_VERBOSE );
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
        Resource resource = getResource();
        Context context = getContext();
        
        try
        {
            context.init();
        }
        catch( IOException ioe )
        {
            final String error = 
              "Context initialization failure for the resource ["
              + resource 
              + "] ("
              + resource.getBaseDir()
              + ").";
            throw new BuilderError( error, ioe );
        }
        
        log( "basedir: " + getResource().getBaseDir(), Project.MSG_VERBOSE );
        
        //
        // setup the file system
        //
        
        String filters = context.getProperty( SRC_FILTERED_INCLUDES_KEY, SRC_FILTERED_INCLUDES_VALUE );
        if( resource.getTypes().length > 0 )
        {
            mkDir( context.getTargetDirectory() );
        }
        if( context.getSrcMainDirectory().exists() )
        {
            log( "preparing 'main' src.", Project.MSG_VERBOSE );
            File src = context.getSrcMainDirectory();
            File dest = context.getTargetBuildMainDirectory();
            mkDir( dest );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/*.*", filters );
        }
        else
        {
            log( "project does not contain 'main' src.", Project.MSG_VERBOSE );
        }
        if( context.getSrcDocsDirectory().exists() )
        {
            log( "preparing 'docs' src.", Project.MSG_VERBOSE );
            File src = context.getSrcDocsDirectory();
            File dest = context.getTargetBuildDocsDirectory();
            mkDir( dest );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/*.*", filters );
        }
        else
        {
            log( "project does not contain 'doc' src.", Project.MSG_VERBOSE );
        }
        if( context.getSrcTestDirectory().exists() )
        {
            log( "preparing 'test' src.", Project.MSG_VERBOSE );
            File src = context.getSrcTestDirectory();
            File dest = context.getTargetDirectory( "build/test" );
            mkDir( dest );
            copy( src, dest, true, filters, "" );
            copy( src, dest, false, "**/*.*", filters );
            File test = context.getTargetDirectory( "test" );
            mkDir( test );
        }
        else
        {
            log( "project does not contain 'test' src.", Project.MSG_VERBOSE );
        }
        
        if( context.getEtcMainDirectory().exists() )
        {
            final String includes = 
              context.getProperty( ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            final String excludes = 
              context.getProperty( ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            
            //
            // copy ${etc}/main content to ${target}/build/main
            //
            
            final File etcMain = context.getEtcMainDirectory();
            final File buildMainDir = context.getTargetDirectory( "build/main" );
            copy( etcMain, buildMainDir, true, includes, excludes );
            copy( etcMain, buildMainDir, false, excludes, "" );
        }
        
        if( context.getEtcTestDirectory().exists() )
        {
            final String includes = 
              context.getProperty( ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            final String excludes = 
              context.getProperty( ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            
            //
            // copy ${etc}/test content to ${target}/build/test
            //
            
            final File etcTest = context.getEtcTestDirectory();
            final File test = context.getTargetDirectory( "build/test" );
            copy( etcTest, test, true, includes, excludes );
            copy( etcTest, test, false, excludes, "" );
        }
        
        if( context.getEtcDataDirectory().exists() )
        {
            final String includes = 
              context.getProperty( ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            final String excludes = 
              context.getProperty( ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            
            //
            // copy ${etc}/test content to ${target}/test
            //
            
            final File data = context.getEtcDataDirectory();
            final File test = context.getTargetDirectory( "test" );
            copy( data, test, true, includes, excludes );
            copy( data, test, false, excludes, "" );
        }

        if( context.getEtcDirectory().exists() )
        {
            final String includes = 
              context.getProperty( ETC_FILTERED_INCLUDES_KEY, ETC_FILTERED_INCLUDES_VALUE );
            final String excludes = 
              context.getProperty( ETC_FILTERED_EXCLUDES_KEY, ETC_FILTERED_EXCLUDES_VALUE );
            final File etc = context.getEtcDirectory();
            
            //
            //  ${etc}/* directories (excluding main, test, and data)
            // directly to the target directory
            //

            String main = context.getEtcMainDirectory().toString() + "/**";
            String test = context.getEtcTestDirectory().toString() + "/**";
            String data = context.getEtcDataDirectory().toString() + "/**";
            File target = context.getTargetDirectory();
            final String standard = main + "," + test + "," + data;
            copy( etc, target, true, includes, standard + "," + excludes );
            copy( etc, target, false, excludes, standard );
        }
    }
}
