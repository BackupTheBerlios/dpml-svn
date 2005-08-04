/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.tasks;

import net.dpml.magic.UnknownResourceException;
import net.dpml.magic.model.Definition;
import net.dpml.transit.Repository;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import java.io.File;


/**
 * Abstract task suitable for definition based tasks.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class ProjectTask extends ContextualTask
{

    //------------------------------------------------------------------
    // ProjectTask
    //------------------------------------------------------------------

   /**
    * Return the working cache directory.
    * @return the cache directory
    */
    public File getCacheDirectory()
    {
        return getIndex().getCacheDirectory();
    }

   /**
    * Return the transit repository service reference.
    * @return the repository service
    */
    public Repository getRepository()
    {
        return getIndex().getRepository();
    }

   /**
    * Returns the project definition for the project.  If the key does not
    * refer to a know project and UnknownResourceException will be thrown.
    *
    * @return the project definition
    * @exception UnknownResourceException if the key does not refer to a project
    */
    public Definition getDefinition() throws UnknownResourceException
    {
        return getContext().getDefinition();
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

   /**
    * Utility operation to copy a fileset to a destination directory.
    * @param destination the destination directory
    * @param fileset the fileset to copy
    * @param preserve the preserve timestamp flag
    */
    public void copy( final File destination, final FileSet fileset, boolean preserve )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setPreserveLastModified( preserve );
        copy.setTodir( destination );
        copy.addFileset( fileset );
        copy.setOverwrite( true );
        copy.init();
        copy.execute();
    }
}
