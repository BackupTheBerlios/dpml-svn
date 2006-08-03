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
import java.io.Writer;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import net.dpml.lang.Version;

import net.dpml.library.Module;
import net.dpml.library.Resource;
import net.dpml.library.Type;

import net.dpml.transit.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.link.ArtifactLinkManager;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Checksum;

/**
 * Execute the install phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InstallTask extends GenericTask
{    
   /**
    * Execute the project.
    * @exception BuildException if a build errror occurs
    */
    public void execute() throws BuildException
    {
        installDeliverables();
    }

    private void installDeliverables()
    {
        Resource resource = getResource();
        String resourceVersion = resource.getVersion();
        boolean snapshot = "SNAPSHOT".equals( resourceVersion );
        boolean bootstrap = "BOOTSTRAP".equals( resourceVersion );
        boolean validation = resource.getBooleanProperty( "project.validation.enabled", false );
        Type[] types = resource.getTypes();
        if( types.length == 0 )
        {
            return;
        }
        
        final File deliverables = getContext().getTargetDeliverablesDirectory();
        for( int i=0; i < types.length; i++ )
        {
            Type type = types[i];
            
            //
            // Check that the project has actually built the resource
            // type that it declares
            //

            String id = type.getID();
            String filename = getContext().getLayoutFilename( id );
            File group = new File( deliverables, id + "s" );
            File target = new File( group, filename );
            if( !target.exists() && !id.equalsIgnoreCase( "null" ) )
            {
                final String error = 
                  "Project [" 
                  + resource 
                  + "] declares that it produces the resource type ["
                  + id
                  + "] however no artifacts of that type are present in the target deliverables directory.";
                throw new BuildException( error, getLocation() );
            }
            
            if( !snapshot && !bootstrap && validation )
            {
                try
                {
                    Artifact artifact = resource.getArtifact( id );
                    URL url = artifact.toURL();
                    File file = (File) url.getContent( new Class[]{File.class} );
                    if( file.exists() )
                    {
                        log( "validating " + target.getName() );
                        compare( file, target, id );
                    }
                }
                catch( ArtifactNotFoundException anfe )
                {
                    // continue as there is nothing to compare with
                }
                catch( IOException ioe )
                {
                    final String error =
                      "IO error while attempting to cross-check resource type: " + id
                      + "\n" + ioe.toString();
                    throw new BuildException( error, ioe, getLocation() );
                }
            }
            
            //
            // If the type declares an alias then construct a link 
            // and add the link to the deliverables directory as part of 
            // install process.
            //

            Version version = type.getVersion();
            if( null != version )
            {
                try
                {
                    Artifact artifact = resource.getArtifact( id );
                    String uri = artifact.toURI().toASCIIString();
                    
                    String link = null;
                    if( Version.NULL_VERSION.equals( version ) )
                    {
                        link = resource.getName() + "." + id + ".link";
                    }
                    else
                    {
                        link = resource.getName()
                        + "-"
                        + version.getMajor()
                        + "." 
                        + version.getMinor()
                        + "."
                        + id + ".link";
                    }
                    File out = new File( group, link );
                    boolean flag = true;
                    if( out.exists() )
                    {
                        ArtifactLinkManager manager = new ArtifactLinkManager();
                        URI enclosed = manager.getTargetURI( new URI( out.toURL().toString() ) );
                        if( artifact.toURI().equals( enclosed ) )
                        {
                            flag = false;
                        }
                    }
                    
                    if( flag )
                    {
                        log( link.toString() );
                        log( uri.toString() );
                        out.createNewFile();
                        final OutputStream output = new FileOutputStream( out );
                        final Writer writer = new OutputStreamWriter( output );
                        writer.write( uri );
                        writer.close();
                        output.close();
                    }
                }
                catch( Exception e )
                {
                    final String error = 
                      "Internal error while attempting to create a link for the resource type ["
                      + id 
                      + "] in project ["
                      + resource
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
                Module parent = resource.getParent();
                if( null == parent )
                {
                    copy( cache, fileset, true );
                }
                else
                {
                    final String group = parent.getResourcePath();
                    final File destination = new File( cache, group );
                    copy( destination, fileset, true );
                }
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
    
    private void compare( File old, File target, String id )
    {
        String oldValue = getChecksum( old );
        String newValue = getChecksum( target );
        if( !oldValue.equals( newValue ) )
        {
            String path = getContext().getLayoutFilename( id );
            final String error =
              "A versioned resource created in this build has a different MD5 signature "
              + "compared to an existing resource of the same name in the cache directory. "
              + "If the cached resource is a published resource a possibility exists that "
              + "this build artifact will be introducing a modification to an existing published "
              + "contract. If the resource has not been published then you can rebuild without "
              + "deliverable validation.  Otherwise, consider assigning an alternative "
              + "(non-conflicting) version identifier."
              + "\n"
              + "\n\tProduced Type: " + path
              + "\n\tCached Resource: " + getCanonicalPath( old )
              + "\n";
            throw new BuildException( error, getLocation() );
        }
    }
    
    private String getCanonicalPath( File file )
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch( IOException e )
        {
            final String error = 
              "Internal error while attempting to resolve a canonical path for the file: " + file;
            throw new BuildException( error, e, getLocation() );
        }
    }
    
    private String getChecksum( File file )
    {
        final String key = "checksum.property." + file.toString();
        final Checksum checksum = (Checksum) getProject().createTask( "checksum" );
        checksum.setTaskName( getTaskName() );
        checksum.setFile( file );
        checksum.setProperty( key );
        checksum.init();
        checksum.execute();
        return getProject().getProperty( key );
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
        copy.setOverwrite( true ); // required for filtered deliverables
        copy.init();
        copy.execute();
    }
}
