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

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import net.dpml.magic.model.Definition;
import net.dpml.magic.project.Context;
import net.dpml.magic.project.DeliverableHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

import java.io.File;

/**
 * Create a block archive.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class BarTask extends ProjectTask
{
    public static final String BAR_EXT = "bar";

    private List m_zipFilesets = new LinkedList();

    private List m_filesets = new LinkedList();

    private boolean m_classic = true;

    public void addZipFileset( ZipFileSet set )
    {
        m_zipFilesets.add( set );
    }

    public void addFileset( FileSet set )
    {
        m_filesets.add( set );
    }

    public void execute() throws BuildException
    {
        final String filename = getDefinition().getFilename( BAR_EXT );

        if( isContent() )
        {
            bar( filename );
        }
    }

    private boolean isContent()
    {
        if( m_classic && getContext().getDeliverablesDirectory().exists() )
        {
             return true;
        }
        else if( m_filesets.size() > 0 )
        {
             return true;
        }
        else if( m_zipFilesets.size() > 0 )
        {
             return true;
        }
        else
        {
             return false;
        }
    }

    private void bar( final String filename )
    {
        final File deliverables = getContext().getDeliverablesDirectory();
        final File bars= new File( deliverables, BAR_EXT + "s" );
        final File bar = new File( bars, filename );
        long modified = -1;
        if( bar.exists() )
        {
            modified = bar.lastModified();
        }

        final Jar jar = (Jar) getProject().createTask( "jar" );
        jar.setTaskName( getTaskName() );
        jar.setDestFile( bar );
        jar.setIndex( true );
        addManifest( jar );
        log( "creating: " + bar );
        bars.mkdirs();

        if( m_classic )
        {
            final FileSet fileset = new FileSet();
            fileset.setDir( deliverables );
            fileset.createInclude().setName( "**/*" );
            fileset.createExclude().setName( "**/" + BAR_EXT + "s" );
            fileset.createExclude().setName( "**/" + BAR_EXT + "s/*.*" );
            ZipFileSet deliverablesSet = new BarZipFileSet( fileset );
            deliverablesSet.setPrefix( getDefinition().getInfo().getGroup() );
            jar.addZipfileset( deliverablesSet );
        }

        Iterator zipIterator = m_zipFilesets.iterator();
        while( zipIterator.hasNext() )
        {
            ZipFileSet set = (ZipFileSet) zipIterator.next();
            jar.addZipfileset( set );
        }

        Iterator filesetIterator = m_filesets.iterator();
        while( filesetIterator.hasNext() )
        {
            FileSet set = (FileSet) filesetIterator.next();
            jar.addFileset( set );
        }

        jar.init();
        jar.execute();

        if( bar.lastModified() > modified )
        {
            DeliverableHelper.checksum( this, bar );
            String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
            DeliverableHelper.asc( this, bar, gpg );
        }
    }

    private void addManifest( final Jar jar )
    {
        Definition def = getDefinition();
        try
        {
            final Manifest manifest = new Manifest();
            final Manifest.Section main = manifest.getMainSection();
            addAttribute( main, "Created-By", "The Digital Product Meta Library" );
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

    private static class BarZipFileSet extends ZipFileSet
    {
        private BarZipFileSet( FileSet set )
        {
            super( set );
        }
    }
}
