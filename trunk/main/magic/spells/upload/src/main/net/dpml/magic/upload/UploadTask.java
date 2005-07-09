/* 
 * Copyright 2004 Niclas Hedhman
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

package net.dpml.magic.upload;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;

import net.dpml.magic.model.Definition;
import net.dpml.magic.tasks.ProjectTask;

public class UploadTask extends ProjectTask
{
    private static final String KEY_PUBLISH_USER_NAME = "project.publish.username";
    private static final String KEY_PUBLISH_AUTHORATIVE_DESTINATION = "project.publish.authorative.destination";
    
    public void init() throws BuildException 
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty( 
              KEY_PUBLISH_AUTHORATIVE_DESTINATION, "login.ibiblio.org:/public/html/dpml" );
        }
    }

    public void execute()
    {
        Project project = getProject();
        File deliverables = getContext().getDeliverablesDirectory();
        Definition def = getDefinition();
        String type = def.getInfo().getType();
        File types = new File( deliverables, type + "s" );
        String filename = def.getFilename();
        File artifactFile = new File( types, filename );
        
        String username = project.getProperty( KEY_PUBLISH_USER_NAME );
        File[] files = new File[3];
        files[0] = artifactFile;
        files[1] = new File( artifactFile.getAbsolutePath() + ".md5" );
        files[2] = new File( artifactFile.getAbsolutePath() + ".asc" );
        String root = project.getProperty( KEY_PUBLISH_AUTHORATIVE_DESTINATION );
        String destination = root + "/" + def.getInfo().getGroup() + "/" + type;
        FileSet fileset = createFileSet( files );
        copy( destination, fileset, username );        
    }

    private FileSet createFileSet( File[] files )
    {
        final FileSet fileset = new FileSet();
        fileset.setDir( new File( "/" ) );
        StringBuffer includes = new StringBuffer();
        for( int i = 0 ; i < files.length ; i++ )
        {
            if( i != 0 )
                includes.append( ", " );
            includes.append( files[ i ].getAbsolutePath() );
        }
        fileset.setIncludes( includes.toString() );
        includes.setLength( 0 );
        return fileset;
    }
    
    private void copy( String destination, FileSet fileset, String username )
    {
        Project project = getProject();
        project.addTaskDefinition( "scp", Scp.class );
        final Scp scp = (Scp) project.createTask( "scp" );
        log( "Command: " + scp );
        scp.setRemoteTofile( destination );
        scp.addFileset( fileset );
        scp.setUsername( username );
        scp.execute();
    }
}
