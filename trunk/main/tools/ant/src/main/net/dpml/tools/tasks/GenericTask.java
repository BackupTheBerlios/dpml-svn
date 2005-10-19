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
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.ExecTask;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.ant.Context;
import net.dpml.tools.model.Library;

/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class GenericTask extends Task
{   
   /**
    * Constant key for the GPG exe name.
    */
    public static final String GPG_EXE_KEY = "project.gpg.exe";

   /**
    * MD5 file type.
    */
    public static final String MD5_EXT = "md5";

   /**
    * ASC file type.
    */
    public static final String ASC_EXT = "asc";

    private boolean m_init = false;
    
    public void init()
    {
        if( !m_init )
        {
            super.init();
            m_init = true;
        }
    }
    
    protected boolean isInitialized()
    {
        return m_init;
    }
    
   /**
    * Get the project definition.
    */
    protected Definition getDefinition()
    {
        return getContext().getDefinition();
    }
    
   /**
    * Get the library.
    */
    protected Library getLibrary()
    {
        return getContext().getLibrary();
    }
    
   /**
    * Get the project definition.
    */
    protected Context getContext()
    {
        Context context = (Context) getProject().getReference( "project.context" );
        if( null == context )
        {
            final String error = 
              "Missing project context reference.";
            throw new IllegalStateException( error );
        }
        return context;
    }
    
   /**
    * Utility operation to create a new directory if it does not exist.
    * @param dir the directory to create
    */
    protected void mkDir( final File dir )
    {
        final Mkdir mkdir = (Mkdir) getProject().createTask( "mkdir" );
        mkdir.setTaskName( getTaskName() );
        mkdir.setDir( dir );
        mkdir.init();
        mkdir.execute();
    }
    
    protected void copy(
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
    
   /**
    * Utility operation to delete a directory .
    * @param dir the directory to delete
    */
    protected void deleteDir( final File dir )
    {
        final Delete task = (Delete) getProject().createTask( "delete" );
        task.setTaskName( getTaskName() );
        task.setDir( dir );
        task.init();
        task.execute();
    }
    
   /**
    * Create an MD5 checksum file relative to the supplied file.
    * If an [filename].md5 file exists it will be deleted and a new
    * MD5 created.
    *
    * @param file the file from which a checksum signature will be generated
    */
    public void checksum( final File file )
    {
        log( "Creating md5 checksum" );

        final File md5 = new File( file.toString() + "." + MD5_EXT );
        if( md5.exists() )
        {
            md5.delete();
        }

        final Checksum checksum = (Checksum) getProject().createTask( "checksum" );
        checksum.setTaskName( getTaskName() );
        checksum.setFile( file );
        checksum.setFileext( "." + MD5_EXT );
        checksum.init();
        checksum.execute();
    }

   /**
    * Creation of an ASC signature relative to a supplied file.  If a [filename].asc
    * exists it will be deleted and recreated relative to the supplied file content.
    *
    * @param file the file to sign
    */
    public void asc( final File file )
    {
        final String path = Project.translatePath( file.toString() );
        final File asc = new File( file.toString() + "." + ASC_EXT );
        if( asc.exists() )
        {
            asc.delete();
        }

        String gpg = getProject().getProperty( GPG_EXE_KEY );
        if( ( null != gpg ) && !"".equals( gpg ) )
        {
            log( "Creating asc signature using '" + gpg + "'." );
            final ExecTask execute = (ExecTask) getProject().createTask( "exec" );

            execute.setExecutable( gpg );

            execute.createArg().setValue( "-a" );
            execute.createArg().setValue( "-b" );
            execute.createArg().setValue( "-o" );
            execute.createArg().setValue( path + "." + ASC_EXT );
            execute.createArg().setValue( path );

            execute.setDir( getProject().getBaseDir() );
            execute.setSpawn( false );
            execute.setAppend( false );
            execute.setTimeout( new Integer( TIMEOUT ) );
            execute.execute();
        }
    }

    private static final int TIMEOUT = 10000;


}
