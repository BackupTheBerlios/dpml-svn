/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.transit.tools;

import java.io.File;
import java.net.URL;
import java.net.URI;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ImportTask;

import net.dpml.transit.Artifact;
import net.dpml.transit.util.PropertyResolver;

/**
 * Ant task that provides support for the import of build file templates
 * via an artifact url.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ImportArtifactTask extends ImportTask
{
   /**
    * A flag indicating that nested directives have been provided.
    */
    private boolean m_flag = false;

   /**
    * Set the project.
    * @param project the current project
    */
    public void setProject( Project project )
    {
        super.setProject( project );
        setTaskName( "import" );
        TransitTask.initialize( this );
    }

    // ------------------------------------------------------------------------
    // Task
    // ------------------------------------------------------------------------

   /**
    * Set the file to import. Any symbolic references in the supplied 
    * file argument will be resolved prior to invoking the standard setFile
    * operation.
    *
    * @param file the template filename
    */
    public void setFile( String file )
    {
        String path = PropertyResolver.resolve( file );
        super.setFile( path );
    }

   /**
    * Set the artifact to import.
    * @param uri the artifact to import into the build file
    * @exception BuildException if an error occurs while attempting to
    *   resolve the artifact uri
    */
    public void setUri( URI uri ) throws BuildException
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( uri );
            URL url = artifact.toURL();
            File local = (File) url.getContent( new Class[]{File.class} );
            super.setFile( local.getAbsolutePath() );
        }
        catch( Exception e )
        {
            final String error =
              "Could not import the resource from the uri ["
              + uri
              + "]";
            throw new BuildException( error, e );
        }
    }
    
    public void execute() 
    {
        try
        {
            super.execute();
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Input (super).execute() failed.";
            throw new BuildException( error, e );
        }
    }
}

