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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Writer;

import net.dpml.magic.model.Type;
import net.dpml.magic.model.Definition;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Install the ${basedir}/target/deliverables content into the local
 * repository cache.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class InstallTask extends ProjectTask
{
    private String m_id;

   /**
    * Set the project definiton id.
    * @param id the defintion id
    */
    public void setId( final String id )
    {
        m_id = id;
    }

   /**
    * Execute the project.
    * @exception BuildException if a build errror occurs
    */
    public void execute() throws BuildException
    {
        final Definition definition = getReferenceDefinition();
        installDeliverables( definition );
    }

    private Definition getReferenceDefinition()
    {
        if( null != m_id )
        {
            return getIndex().getDefinition( m_id );
        }
        else
        {
            return getIndex().getDefinition( getContext().getKey() );
        }
    }

    private void installDeliverables( final Definition definition )
    {
        final File deliverables = getContext().getDeliverablesDirectory();
        Definition def = getReferenceDefinition();
        Type[] types = def.getInfo().getTypes();
        for( int i=0; i < types.length; i++ )
        {
            //
            // Check that the project has actually built the resource
            // type that it declares
            //

            Type type = types[i];
            String name = type.getName();
            String filename = def.getInfo().getFilename( name );
            File group = new File( deliverables, name + "s" );
            File target = new File( group, filename );
            if( !target.exists() && !name.equalsIgnoreCase( "null" ) )
            {
                final String error = 
                  "Project " 
                  + def 
                  + " declares that it produces the resource type ["
                  + name 
                  + "] however no artifacts of that type are present in the target/deliverables directory.";
                throw new BuildException( error, getLocation() );
            }

            //
            // If the type declares an alias then construct a link 
            // and add the link to the deliverables directory as part of 
            // install process.
            //

            String alias = type.getAlias();
            if( null != alias )
            {
                String uri = def.getInfo().getURI( name );
                String link = def.getInfo().getLinkFilename( type );

                final String message = 
                   "Creating link ["
                   + link
                   + "] with uri ["
                   + uri
                   + "]";
                log( message );

                File out = new File( group, link );
                try
                {
                    out.createNewFile();
                    final OutputStream output = new FileOutputStream( out );
                    final Writer writer = new OutputStreamWriter( output );
                    writer.write( uri );
                    writer.close();
                    output.close();
               }
               catch( IOException e )
               {
                   final String error = 
                     "Internal error while attempting to create a link for the resource type ["
                     + name 
                     + "] for the project "
                     + def;
                   throw new BuildException( error, e, getLocation() );
               }
            }
        }

        if( deliverables.exists() )
        {
            log( "Installing deliverables from [" + deliverables + "]", Project.MSG_VERBOSE );
            final File cache = getCacheDirectory();
            log( "To cache dir [" + cache + "]", Project.MSG_VERBOSE );
            try
            {
                final FileSet fileset = new FileSet();

                //
                // WARNING: Ant is kind of brittle - if you do a 
                // fileset.toString() you will break things with an NPE
                // (ant 1.6.4)
                //

                fileset.setDir( deliverables );
                fileset.createInclude().setName( "**/*" );
                final String group = definition.getInfo().getGroup();
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
}
