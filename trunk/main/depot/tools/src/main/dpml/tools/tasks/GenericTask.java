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

import dpml.util.StandardClassLoader;
import dpml.library.impl.InvalidNameException;

import java.io.File;

import dpml.tools.Context;
import dpml.tools.impl.DefaultContext;

import dpml.library.Library;
import dpml.library.Resource;
import dpml.library.ResourceNotFoundException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.util.FileUtils;


/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
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
    
   /**
    * Initialize the task.
    */
    public void init()
    {
        if( !m_init )
        {
            super.init();
            m_init = true;
        }
    }
    
   /**
    * Return the initialized state of the task.
    * @return true if initialized
    */
    protected boolean isInitialized()
    {
        return m_init;
    }
    
   /**
    * Get the project definition.
    * @return the resource
    */
    protected Resource getResource()
    {
        return getContext().getResource();
    }
    
   /**
    * Get the library.
    * @return the library
    */
    protected Library getLibrary()
    {
        return getContext().getLibrary();
    }
    
   /**
    * Get the project context.
    * @return the project context
    */
    public Context getContext()
    {
        Project project = getProject();
        Object object = project.getReference( "project.context" );
        if( object != null )
        {
            if( object instanceof Context )
            {
                return (Context) object;
            }
            else
            {
                final String classpath = StandardClassLoader.toString( 
                      object.getClass().getClassLoader(),
                      Context.class.getClassLoader() );
                      
                final String error = 
                  "Object assigned under the 'project.context' key is not assignable to a context instance."
                  + "\nObject: " + object.getClass().getName()
                  + "\nExpecting: " + Context.class.getName()
                  + classpath;
                  
                throw new IllegalStateException( error );
            }
        }
        else
        {
            // this is an Ant initiated bootstrapping of Depot (i.e. we are 
            // not using a project established via the default builder) as such
            // we need to declare the build signature argument and construct the
            // project context
            
            String signature = project.getProperty( "build.signature" );
            if( null != signature )
            {
                System.setProperty( "build.signature", signature );
            }
            
            try
            {
                return new DefaultContext( project );
            }
            catch( InvalidNameException e )
            {
                String error = e.getMessage();
                throw new BuildException( error, e, getLocation() );
            }
            catch( ResourceNotFoundException e )
            {
                final String message = e.getMessage();
                final String error = "Resource not found: " + message;
                throw new BuildException( error, e, getLocation() );
            }
            catch( BuildException e )
            {
                throw e;
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to construct project context.";
                throw new RuntimeException( error, e );
            }
        }
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
    
   /**
    * Utility operation to copy a file from a source to a destination.
    * @param src the src file
    * @param destination the destination file
    * @param filtering if true apply filtering during the copy
    * @param includes the includes specification
    * @param excludes the excludes specification
    */
    protected void copy(
       final File src, final File destination, final boolean filtering, 
       final String includes, final String excludes )
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
        final String path = FileUtils.translatePath( file.toString() );
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
    
   /**
    * Return a property value.
    * @param key the property key
    * @param value tyhe default value
    * @return the property value
    */
    protected String getProperty( String key, String value )
    {
        String v = getProject().getProperty( key );
        if( null != v )
        {
            return v;
        }
        else
        {
            return getResource().getProperty( key, value );
        }
    }

    private static final int TIMEOUT = 10000;
}
