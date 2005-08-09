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

import java.io.File;
import java.util.ArrayList;

import net.dpml.magic.project.ProjectPath;
import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Policy;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


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
    private boolean m_self = false;
    private boolean m_verbose = false;

   /**
    * Set the flattern policy.
    * @param flag the flattern policy
    */
    public void setFlatten( boolean flag )
    {
        m_flatten = flag;
    }

   /**
    * Set the self inclusion policy.
    * @param flag the self flag
    */
    public void setSelf( boolean flag )
    {
        m_self = flag;
    }

   /**
    * Set the verbose policy.
    * @param flag the verbose flag
    */
    public void setVerbose( boolean flag )
    {
        m_verbose = flag;
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
            Definition def = getDefinition();
            Project project = getProject();
            m_path = def.getPath( project, Policy.RUNTIME, "*", true, m_self );
        }
        if( null == m_todir )
        {
            m_todir = new File( getContext().getTargetDirectory(), "replicate" );
        }
        File cache = getCacheDirectory();
        FileSet[] filesets = createFileSets( cache, m_path );
        for( int i=0; i<filesets.length; i++ )
        {
            FileSet fileset = filesets[i];
            copy( m_todir, fileset );
        }
    }

    private FileSet[] createFileSets( final File cache, final Path path )
    {
        getProject().log( "using replication path: " + m_path, Project.MSG_VERBOSE );

        ArrayList list = new ArrayList();

        final FileSet cacheset = new FileSet();
        cacheset.setDir( cache );
        list.add( cacheset );

        final File deliverables = getContext().getDeliverablesDirectory();
        final FileSet localset = new FileSet();
        localset.setDir( deliverables );
        list.add( localset );

        String root = cache.toString();
        String local = deliverables.toString();

        int count = 0;
        log( "Constructing repository based fileset", Project.MSG_VERBOSE );
        String[] translation = path.list();
        for( int i=0; i < translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                String relativeFilename = trans.substring( root.length() + 1 );
                if( m_verbose )
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename );
                }
                else
                {
                    log( "${dpml.cache}" + File.separator + relativeFilename, Project.MSG_VERBOSE );
                }
                cacheset.createInclude().setName( relativeFilename );
                cacheset.createInclude().setName( relativeFilename + ".*" );
                cacheset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
            else if( trans.startsWith( local ) )
            {
                String relativeFilename = trans.substring( local.length() + 1 );
                if( m_verbose )
                {
                    log( "${project.deliverables}" + File.separator + relativeFilename );
                }
                else
                {
                    log( "${project.deliverables}" + File.separator + relativeFilename, Project.MSG_VERBOSE );
                }
                localset.createInclude().setName( relativeFilename );
                localset.createInclude().setName( relativeFilename + ".*" );
                localset.createInclude().setName( relativeFilename + ".*.*" );
                count++;
            }
            else
            {
                if( m_verbose )
                {
                    log( "including: " + trans );
                }
                else
                {
                    log( "including: " + trans, Project.MSG_VERBOSE );
                }

                FileSet fileset = new FileSet();
                File file = new File( trans );
                fileset.setFile( file );
                list.add( fileset );
                String filename = file.getName();
                fileset.createInclude().setName( filename + ".*" );
                fileset.createInclude().setName( filename + ".*.*" );
                count++;
            }
        }
        log( "entries: " + count );
        return (FileSet[]) list.toArray( new FileSet[0] );
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
