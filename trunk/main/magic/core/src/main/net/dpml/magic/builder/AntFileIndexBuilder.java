/*
 * Copyright 2004-2005 Stephen McConnell
 * Copyright 2005      Niclas Hedhman
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

package net.dpml.magic.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.dpml.magic.AntFileIndex;

import net.dpml.transit.NullArgumentException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;


/**
 * The Magic class is the application root of the magic system.  It is responsible
 * for the onetime establishment of an index.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class AntFileIndexBuilder extends DataType
{
    //-------------------------------------------------------------
    // static
    //-------------------------------------------------------------

   /**
    * Immutable key to the signature identifier used.
    */
    private static final String RELEASE_SIGNATURE_KEY = "dpml.release.signature";

   /**
    * Immutable key to the build identifier.
    */
    private static final String BUILD_SIGNATURE_KEY = "dpml.build.signature";

   /**
    * Immutable key to the magic docs cache directory.
    */
    public static final String DOCS_KEY = "magic.docs";

    //-------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------

    private final AntFileIndex m_index;

    //-------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------

   /**
    * Creation of a new ant file index builder.
    * @param proj the ant project
    * @exception BuildException if a build error occurs
    */
    public AntFileIndexBuilder( final Project proj )
        throws BuildException
    {
        this( proj, null );
    }

   /**
    * Creation of a new ant file index builder.
    * @param proj the ant project
    * @param index the index file
    * @exception BuildException if a build error occurs
    */
    public AntFileIndexBuilder( final Project proj, File index )
        throws BuildException
    {
        setProject( proj );

        log( "magic builder established", Project.MSG_VERBOSE );

        String signature = getBuildSignature();

        if( null == signature )
        {
            signature = "SNAPSHOT";
        }

        proj.setProperty( BUILD_SIGNATURE_KEY, signature );
        log( "release: " + signature, Project.MSG_VERBOSE );

        if( null == index )
        {
            log( "locating index", Project.MSG_VERBOSE );

            File file = getIndexFile( proj );
            m_index = constructIndex( proj, file );

        }
        else
        {
            m_index = constructIndex( proj, index );
        }
    }

    private AntFileIndex constructIndex( Project proj, File file )
    {
        try
        {
            return new AntFileIndex( proj, this, file );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            final String error =
              "Internal error while attempting to construct index."
              + "file:" + file.getAbsolutePath()
              + "project:" + proj.toString();
            throw new BuildException( error, e );
        }
    }

    //-------------------------------------------------------------
    // public
    //-------------------------------------------------------------

   /**
    * Return the index.
    * @return the index instance
    */
    public AntFileIndex getIndex()
    {
        return m_index;
    }

   /**
    * Return TRUE if this is a release build.
    * @return the release build flag
    */
    public boolean isaRelease()
    {
        String signature = getReleaseSignature();
        return signature != null;
    }

   /**
    * Return the signature.
    *
    * @return the release signature
    */
    public String getReleaseSignature()
    {
        Project project = getProject();
        return project.getProperty( BUILD_SIGNATURE_KEY );
    }

    //-------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------

    private String getBuildSignature()
    {
        String signature = System.getProperty( BUILD_SIGNATURE_KEY );
        if( null != signature )
        {
            return signature;
        }
        else
        {
            Project proj = getProject();
            signature = proj.getProperty( RELEASE_SIGNATURE_KEY );
            return signature;
        }
    }

    private File getIndexFile( Project proj )
    {
        File basedir = proj.getBaseDir();
        return resolve( basedir, true );
    }

    private File resolve( File index, boolean traverse )
    {
        if( index.isFile() )
        {
            return index;
        }
        if( index.isDirectory() )
        {
            File file = new File( index, "index.xml" );
            if( file.isFile() )
            {
                return file;
            }
            if( traverse )
            {
                File resolved = traverse( index );
                if( resolved != null )
                {
                    return resolved;
                }
            }
        }

        final String error = 
          "Unable to locate an 'index.xml' file relative to the project directory ["
          + getProject().getBaseDir()
          + "].";
        throw new BuildException( error );
    }

    private File traverse( File dir )
    {
        File file = new File( dir, "index.xml" );
        if( file.isFile() )
        {
            return file;
        }
        File parent = dir.getParentFile();
        if( null != parent )
        {
            return traverse( parent );
        }
        return null;
    }

   /**
    * Return a file using a supplied root and path.  If the path is absolute
    * an absolute file is retured relative to the supplied path otherwise the
    * path is resolved relative to the supplied root directory.
    *
    * @param root the root directory
    * @param path the absolute or relative file path
    * @return the file instance
    */
    public static File getFile( final File root, final String path )
    {
        return getFile( root, path, false );
    }

   /**
    * Return a file using a supplied root and path.  If the path is absolute
    * an absolute file is retured relative to the supplied path otherwise the
    * path is resolved relative to the supplied root directory.  If the create
    * parameter is TRUE then the file will be created if it does not exist.
    *
    * @param root the root directory
    * @param path the absolute or relative file path
    * @param create flag to indicate creation policy if the file does not exists
    * @return the file instance
    * @throws NullArgumentException if the path argument is null, or if the path
    *         argument is not an absolute path and the root argument is null.
    */
    public static File getFile( final File root, final String path, boolean create )
        throws NullArgumentException
    {
        if( null == path )
        {
            throw new NullArgumentException( "path" );
        }
        final File file = new File( path );

        if( file.isAbsolute() )
        {
            return getCanonicalFile( file, create );
        }

        if( null == root )
        {
            throw new NullArgumentException( "root" );
        }
        return getCanonicalFile( new File( root, path ), create );
    }

   /**
    * Return the concatonal variant of a file.
    * @param file the file argument
    * @return the canonical variant
    * @exception BuildException if a build error occurs
    */
    public static File getCanonicalFile( final File file ) throws BuildException
    {
        return getCanonicalFile( file, false );
    }

   /**
    * Return the concatonal variant of a file and ensure that the parent directory
    * path is created.
    *
    * @param file the file argument
    * @param create if TRUE create pararent directories of the supplied file
    * @return the canonical variant
    * @exception BuildException if a build error occurs
    */
    public static File getCanonicalFile( final File file, boolean create ) throws BuildException
    {
        try
        {
            File result = file.getCanonicalFile();
            if( create )
            {
                if( result.isDirectory() )
                {
                    result.mkdirs();
                }
                else
                {
                    result.getParentFile().mkdirs();
                }
            }
            return result;
        }
        catch( IOException ioe )
        {
            throw new BuildException( ioe );
        }
    }

   /**
    * Return the concatonal path of a file.
    * @param file the file argument
    * @return the canonical path
    * @exception BuildException if a build error occurs
    */
    public static String getCanonicalPath( final File file ) throws BuildException
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch( IOException ioe )
        {
            throw new BuildException( ioe );
        }
    }
}
