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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.ExecTask;

import org.apache.avalon.tools.model.Home;

import java.io.File;

/**
 * Utilites supporting the generation of MD5 and ASC artifacts.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class DeliverableHelper
{
    public static final String MD5_EXT = "md5";
    public static final String ASC_EXT = "asc";

   /**
    * Create an MD5 checksum file relative to the supplied file.  
    * If an [filename].md5 file exists it will be deleted and a new 
    * MD5 created.
    * 
    * @param task the task controlling file generation
    * @param file the file from which a checksum signature will be generated
    */
    public static void checksum( final Task task, final File file )
    {
        task.log( "Creating md5 checksum" );

        final File md5 = new File( file.toString() + "." + MD5_EXT );
        if( md5.exists() )
        {
            md5.delete();
        }

        final Checksum checksum = (Checksum) task.getProject().createTask( "checksum" );
        checksum.setTaskName( task.getTaskName() );
        checksum.setFile( file );
        checksum.setFileext( "." + MD5_EXT );
        checksum.init();
        checksum.execute();
    }

   /**
    * Creation of an ASC signature relative to a supplied file.  If a [filename].asc
    * exists it will be deleted and recreated relative to the supplied file content.
    * The ASC signature will be generated using the executable assigned to the property
    * Home.GPG_EXE_KEY.
    *
    * @param home the magic home
    * @param task the task creating the file
    * @param file the file to sign
    */
    public static void asc( final Home home, final Task task, final File file )
    {
        final String path = Project.translatePath( file.toString() );
        final File asc = new File( file.toString() + "." + ASC_EXT );
        if( asc.exists() )
        {
            asc.delete();
        }
        
        final String gpg = home.getProperty( Home.GPG_EXE_KEY );

        if(( null != gpg ) && !"".equals( gpg ) )
        {
            task.log( "Creating asc signature using '" + gpg + "'." );
            final ExecTask execute = (ExecTask) task.getProject().createTask( "exec" );

            execute.setExecutable( gpg );

            execute.createArg().setValue( "-a" );
            execute.createArg().setValue( "-b" );
            execute.createArg().setValue( "-o" );
            execute.createArg().setValue( path + "." + ASC_EXT );
            execute.createArg().setValue( path );

            execute.setDir( task.getProject().getBaseDir() );
            execute.setSpawn( false );
            execute.setAppend( false );
            execute.setTimeout( new Integer( 1000 ) );
            execute.execute();
        }
    }
}
