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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Writer;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.info.ProductionDirective;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * Execute the install phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class InstallTask extends GenericTask
{    
   /**
    * Execute the project.
    * @exception BuildException if a build errror occurs
    */
    public void execute() throws BuildException
    {
        final Definition definition = getDefinition();
        installDeliverables( definition );
    }

    private void installDeliverables( final Definition definition )
    {
        final File deliverables = definition.getTargetDeliverablesDirectory();
        
        ProductionDirective[] types = definition.getProductionDirectives();
        for( int i=0; i < types.length; i++ )
        {
            ProductionDirective type = types[i];
            
            //
            // Check that the project has actually built the resource
            // type that it declares
            //

            String name = type.getType();
            String filename = definition.getLayoutPath( name );
            File group = new File( deliverables, name + "s" );
            File target = new File( group, filename );
            if( !target.exists() && !name.equalsIgnoreCase( "null" ) )
            {
                final String error = 
                  "Project [" 
                  + definition 
                  + "] declares that it produces the resource type ["
                  + name 
                  + "] however no artifacts of that type are present in the target deliverables directory.";
                throw new BuildException( error, getLocation() );
            }

            //
            // If the type declares an alias then construct a link 
            // and add the link to the deliverables directory as part of 
            // install process.
            //

            boolean alias = type.getAlias();
            if( alias )
            {
                try
                {
                    String uri = definition.getArtifactURI( name ).toASCIIString();
                    String link = definition.getName() + "." + name + ".link";
                    final String message = 
                      "Creating alias ["
                      + link
                      + "] with uri ["
                      + uri
                      + "]";
                    log( message );
                    File out = new File( group, link );
                    out.createNewFile();
                    final OutputStream output = new FileOutputStream( out );
                    final Writer writer = new OutputStreamWriter( output );
                    writer.write( uri );
                    writer.close();
                    output.close();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Internal error while attempting to create a link for the resource type ["
                      + name 
                      + "] in project ["
                      + definition
                      + "].";
                    throw new BuildException( error, e, getLocation() );
                }
            }
        }

        if( deliverables.exists() )
        {
            log( "Installing deliverables from [" + deliverables + "]", Project.MSG_VERBOSE );
            final File cache = (File) getProject().getReference( "dpml.cache" );
            log( "To cache dir [" + cache + "]", Project.MSG_VERBOSE );
            try
            {
                final FileSet fileset = new FileSet();
                fileset.setProject( getProject() );
                fileset.setDir( deliverables );
                fileset.createInclude().setName( "**/*" );
                final String group = definition.getGroup();
                final File destination = new File( cache, group );
                copy( destination, fileset, true );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while constructing ant fileset."
                  + "\nDeliverables dir: " + deliverables;
                throw new BuildException( error, e );
            }
        }
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
