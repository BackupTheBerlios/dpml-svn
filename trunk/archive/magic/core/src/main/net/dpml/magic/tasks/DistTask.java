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

import net.dpml.magic.project.Context;
import net.dpml.magic.project.DeliverableHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

import java.io.File;

/**
 * A very primite distribution task.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class DistTask extends ProjectTask
{
    private static final String ZIP_EXT = "zip";
    private static final String TAR_EXT = "tar";
    private static final String DIST_DIRECTORY_NAME = "dist";

    private List m_filesets = new LinkedList();

    private List m_zipFilesets = new LinkedList();

    private TarTask m_tar;

    private String m_compression;

    /**
     * Set compression method.
     * Allowable values are
     * <ul>
     * <li>  none - no compression
     * <li>  gzip - Gzip compression
     * <li>  bzip2 - Bzip2 compression
     * </ul>
     * @param mode the compression method.
     */
    public void setCompression( TarTask.TarCompressionMethod mode )
    {
        if( null == m_tar )
        {
            m_tar = createNewTarTask();
        }
        m_tar.setCompression( mode );
        String policy = mode.getValue();
        if( "gzip".equals( policy ) )
        {
            m_compression = "gz";
        }
        else if( "bzip2".equals( policy ) )
        {
            m_compression = "gz2";
        }
    }

   /**
    * Add a fileset to the distribution.
    * @param set the fileset to add
    */
    public void addFileset( FileSet set )
    {
        m_filesets.add( set );
    }

   /**
    * Add a zipfileset to the distribution.
    * @param set the zipfileset to add
    */
    public void addZipFileset( ZipFileSet set )
    {
        m_zipFilesets.add( set );
    }

   /**
    * Create and add a tarfileset to the distribution.
    * @return a new tarfileset
    */
    public TarTask.TarFileSet createTarFileSet()
    {
        if( null == m_tar )
        {
            m_tar = createNewTarTask();
        }
        return m_tar.createTarFileSet();
    }

   /**
    * Execute the task.
    * @exception BuildException if there is a build failue
    */
    public void execute() throws BuildException
    {
        final File dist = new File( getContext().getTargetDirectory(), DIST_DIRECTORY_NAME );
        deleteDir( dist );
        mkDir( dist );

        if( isZipContentDeclared() )
        {
            final String zipfilename = getDefinition().getFilename( "win32", ZIP_EXT );
            final File zipfile = new File( dist, zipfilename );
            zipIt( zipfile );
        }

        if( isTarContentDeclared() )
        {
            final String tarfilename = getTarFilename();
            final File tarfile = new File( dist, tarfilename );
            tarIt( tarfile );
        }
    }

    private TarTask createNewTarTask()
    {
        TarTask task = new TarTask();
        task.setProject( getProject() );
        return task;
    }

    private String getTarFilename()
    {
        final String tarfilename = getDefinition().getFilename( "linux", TAR_EXT );
        if( m_compression != null )
        {
            return tarfilename + "." + m_compression;
        }
        else
        {
            return tarfilename;
        }
    }

    private void zipIt( File distribution )
    {
        final Zip zip = (Zip) getProject().createTask( "zip" );
        zip.setDestFile( distribution );

        long modified = -1;
        if( distribution.exists() )
        {
            modified = distribution.lastModified();
        }

        Iterator zipIterator = m_zipFilesets.iterator();
        while( zipIterator.hasNext() )
        {
            ZipFileSet set = (ZipFileSet) zipIterator.next();
            zip.addZipfileset( set );
        }

        Iterator filesetIterator = m_filesets.iterator();
        while( filesetIterator.hasNext() )
        {
            FileSet set = (FileSet) filesetIterator.next();
            zip.addFileset( set );
        }

        zip.init();
        zip.execute();

        if( distribution.lastModified() > modified )
        {
            DeliverableHelper.checksum( this, distribution );
            String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
            DeliverableHelper.asc( this, distribution, gpg );
        }
    }

    private void tarIt( File distribution )
    {
        if( null == m_tar )
        {
            m_tar = createNewTarTask();
        }

        m_tar.setDestFile( distribution );

        long modified = -1;
        if( distribution.exists() )
        {
            modified = distribution.lastModified();
        }

        Iterator filesetIterator = m_filesets.iterator();
        while( filesetIterator.hasNext() )
        {
            FileSet set = (FileSet) filesetIterator.next();
            m_tar.addFileset( set );
        }

        TarTask.TarLongFileMode mode = new TarTask.TarLongFileMode();
        mode.setValue( "gnu" );
        m_tar.setLongfile( mode );

        m_tar.init();
        m_tar.execute();

        if( distribution.lastModified() > modified )
        {
            DeliverableHelper.checksum( this, distribution );
            String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
            DeliverableHelper.asc( this, distribution, gpg );
        }
    }

    private boolean isContentDeclared()
    {
        return ( m_filesets.size() > 0 );
    }

    private boolean isZipContentDeclared()
    {
        return ( ( m_zipFilesets.size() > 0 ) || isContentDeclared() );
    }

    private boolean isTarContentDeclared()
    {
        return ( ( null != m_tar ) || isContentDeclared() );
    }
}
