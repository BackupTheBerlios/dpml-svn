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

package net.dpml.tools.ant.tasks;

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

import net.dpml.tools.ant.Definition;

/**
 * Prepare the target build directory based on content presented under the
 * ${basedir}/src and ${basedir}/etc directories.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public abstract class GenericTask extends Task
{
    private boolean m_init = false;
    private Definition m_definition;
    
   /**
    * Task initiaization
    */
    public void init()
    {
        if( m_init )
        {
            return;
        }
        super.init();
        m_definition = (Definition) getProject().getReference( "project.definition" );
        if( null == m_definition )
        {
            final String error = 
              "Missing project definition reference.";
            throw new IllegalStateException( error );
        }
        m_init = true;
    }
    
   /**
    * Get the project definition.
    */
    public Definition getDefinition()
    {
        if( !m_init )
        {
            init();
        }
        return m_definition;
    }
    
   /**
    * Utility operation to create a new directory if it does not exist.
    * @param dir the directory to create
    */
    public void mkDir( final File dir )
    {
        final Mkdir mkdir = (Mkdir) getProject().createTask( "mkdir" );
        mkdir.setTaskName( getTaskName() );
        mkdir.setDir( dir );
        mkdir.init();
        mkdir.execute();
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
    
   /**
    * Utility operation to delete a directory .
    * @param dir the directory to delete
    */
    public void deleteDir( final File dir )
    {
        final Delete task = (Delete) getProject().createTask( "delete" );
        task.setTaskName( getTaskName() );
        task.setDir( dir );
        task.init();
        task.execute();
    }
}
