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
import org.apache.avalon.tools.model.Home;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Mkdir;

import java.io.File;


/**
 * Build a set of projects taking into account cross-project dependencies.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ReplicateTask extends Task
{
    private File m_todir;
    private Path m_path;
    private Context m_context;
    private Home m_home;
    private boolean m_flatten = false;

    public void setFlatten( boolean flag )
    {
        m_flatten = flag;
    }

    public void init()
    {
        if( null == m_context )
        {
            m_context = Context.getContext( getProject() );
        }

        if( null == m_home ) 
        {
            Home home = (Home) getProject().getReference( Home.KEY );
            if( null == home )
            {
                final String error = 
                  "Undefined home.";
                throw new BuildException( error );
            }
            else
            {
                m_home = home;
            }
        }
    }

   /**
    * Set the path programatically.
    * @param path a path object
    */
    public void setReplicationPath( Path path )
    {
        m_path = path;
    }

   /**
    * The id of a repository based path.
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
    */
    public void setTodir( File todir )
    {
        m_todir = todir;
    }

    public void execute()
    {
        if( null == m_path )
        {
            final String error = 
              "Required path id attribute is not declared on replicate task.";
            throw new BuildException( error );
        }
        if( null == m_todir )
        {
            final String error = 
              "Required path id attribute is not declared on replicate task.";
            throw new BuildException( error );
        }

        File cache = getHome().getRepository().getCacheDirectory();
        FileSet fileset = createFileSet( cache, m_path );
        copy( m_todir, fileset );
    }

    private FileSet createFileSet( final File cache, final Path path )
    {
        getProject().log( "using replication path: " + m_path, Project.MSG_VERBOSE );

        String root = cache.toString();
        String sequence = path.toString();
        String[] translation = Path.translatePath( getProject(), sequence );
        final FileSet fileset = new FileSet();
        fileset.setDir( cache );
        log( "Constructing repository based fileset", Project.MSG_VERBOSE );
        for( int i=0; i<translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                String relativeFilename = trans.substring( root.length() + 1 );
                log( relativeFilename, Project.MSG_VERBOSE );
                fileset.createInclude().setName( relativeFilename );
                fileset.createInclude().setName( relativeFilename + ".*" );
            }
        }
        return fileset;
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

    public void mkDir( final File dir )
    {
        final Mkdir mkdir = (Mkdir) getProject().createTask( "mkdir" );
        mkdir.setDir( dir );
        mkdir.init();
        mkdir.execute();
    }

    private Home getHome()
    {
        return m_home;
    }
}
