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

import org.apache.avalon.tools.model.Definition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

/**
 * Load a goal. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class BarTask extends SystemTask
{
    public static final String BAR_EXT = "bar";

    private String m_name;

    public void setName( final String name )
    {
        m_name = name;
    }
    
    private String getName( final Definition def )
    {
        if( null == m_name )
        {
            return def.getFilename( BAR_EXT );
        }
        else
        {
            return m_name;
        }
    }

    public void execute() throws BuildException 
    {
        final File deliverables =
          getContext().getDeliverablesDirectory();
        final Definition def = getHome().getDefinition( getKey() );
        final String filename = getName( def );

        final File types = new File( deliverables, BAR_EXT + "s" );
        final File bar = new File( types, filename );

        if( deliverables.exists() )
        {
            final boolean modified = bar( def, deliverables, bar );
            if( modified )
            {
                DeliverableHelper.checksum( this, bar );
                DeliverableHelper.asc( getHome(), this, bar );
            }
        }
    }

    private boolean bar( final Definition def, final File deliverables, final File bar )
    {
        final File dir = bar.getParentFile();
        mkDir( dir );

        long modified = -1;
        if( bar.exists() )
        {
            modified = bar.lastModified();
        }

        final FileSet fileset = new FileSet();
        fileset.setDir( deliverables );
        fileset.createInclude().setName( "**/*" );
        fileset.createExclude().setName( "**/*." + BAR_EXT + "*" );
 
        final Jar jar = (Jar) getProject().createTask( "jar" );
        jar.setTaskName( getTaskName() );
        jar.setDestFile( bar );
        jar.addFileset( fileset );
        jar.setIndex( true );
        addManifest( jar, def );
        jar.init();
        jar.execute();

        return bar.lastModified() > modified;
    }

    private void addManifest( final Jar jar, final Definition def )
    {
        try
        {
            final Manifest manifest = new Manifest();
            final Manifest.Section main = manifest.getMainSection();
            addAttribute( main, "Created-By", "Apache Avalon" );
            addAttribute( main, "Built-By", System.getProperty( "user.name" ) );    

            final Manifest.Section block = new Manifest.Section();
            block.setName( "Block" );
            addAttribute( block, "Block-Key", def.getKey() );    
            addAttribute( block, "Block-Group", def.getInfo().getGroup() );    
            addAttribute( block, "Block-Name", def.getInfo().getName() );
            if( null != def.getInfo().getVersion() )
            {
                addAttribute( 
                  block, 
                  "Block-Version", 
                  def.getInfo().getVersion() );
            }

            manifest.addConfiguredSection( block );

            jar.addConfiguredManifest( manifest );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private void addAttribute( 
      final Manifest.Section section, final String name, final String value )
      throws ManifestException
    {
        final Manifest.Attribute attribute = new Manifest.Attribute( name, value );
        section.addConfiguredAttribute( attribute );
    }
}
