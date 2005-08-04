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

import net.dpml.magic.project.ProjectPath;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;


/**
 * Build a set of projects taking into account cross-project dependencies.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ReplicateTask extends ProjectTask
{
    private File m_todir;
    private Path m_path;
    private boolean m_flatten = false;

   /**
    * Set the flattern policy.
    * @param flag the flattern policy
    */
    public void setFlatten( boolean flag )
    {
        m_flatten = flag;
    }

   /**
    * The id of a repository based path.
    * @param id the path identifier
    * @exception BuildException if the id does not reference a path, or the path is
    *  already set, or the id references an object that is not a path
    */
    public void setRefid( String id )
        throws BuildException
    {
        if( null != m_path )
        {
            final String error =
              "Path already set.";
            throw new BuildException( error );
        }

        Object ref = getProject().getReference( id );
        if( null == ref )
        {
            final String error =
              "Replication path id [" + id + "] is unknown.";
            throw new BuildException( error );
        }

        if( !( ref instanceof Path ) )
        {
            final String error =
              "Replication path id [" + id + "] does not reference a path "
              + "(class " + ref.getClass().getName() + " is not a Path instance).";
            throw new BuildException( error );
        }

        m_path = (Path) ref;
    }

   /**
    * The target directory to copy cached based path elements to.
    * @param todir the destination directory
    */
    public void setTodir( File todir )
    {
        m_todir = todir;
    }

   /**
    * Execute the task.
    */
    public void execute()
    {
        if( null == m_path )
        {
            ProjectPath path = new ProjectPath( getProject() );
            path.setMode( "RUNTIME" );
            path.setType( "*" );
            m_path = path;
        }
        if( null == m_todir )
        {
            m_todir = new File( getContext().getTargetDirectory(), "replicate" );
        }

        File cache = getCacheDirectory();
        FileSet fileset = createFileSet( cache, m_path );
        if( null == fileset )
        {
            final String message =
              "Supplied path is empty - nothing to replicate.";
            log( message );
        }
        else
        {
            copy( m_todir, fileset );
        }
    }

    private FileSet createFileSet( final File cache, final Path path )
    {
        getProject().log( "using replication path: " + m_path, Project.MSG_VERBOSE );

        String[] translation = path.list();
        String root = cache.toString();

        final FileSet fileset = new FileSet();
        fileset.setDir( cache );

        int count = 0;
        log( "Constructing repository based fileset", Project.MSG_VERBOSE );
        for( int i=0; i < translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                String relativeFilename = trans.substring( root.length() + 1 );
                log( relativeFilename, Project.MSG_VERBOSE );
                fileset.createInclude().setName( relativeFilename );
                fileset.createInclude().setName( relativeFilename + ".*" );
                fileset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
            else
            {
                log( "ignoring " + trans, Project.MSG_VERBOSE );
            }
        }
        if( count > 0 )
        {
            log( "entries: " + count );
            return fileset;
        }
        else
        {
            return null;
        }
    }

    private void copy( final File destination, final FileSet fileset )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setPreserveLastModified( true );
        copy.setFlatten( m_flatten );
        copy.setTodir( destination );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }

}
