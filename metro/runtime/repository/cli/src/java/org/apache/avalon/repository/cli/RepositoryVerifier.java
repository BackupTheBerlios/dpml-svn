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

package org.apache.avalon.repository.cli;

import java.io.File;
import java.io.FileFilter;

import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;


/**
 * Merlin command line handler.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: RepositoryVerifier.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class RepositoryVerifier 
{
    //----------------------------------------------------------
    // static
    //----------------------------------------------------------

    private static Resources REZ =
        ResourceManager.getPackageResources( RepositoryVerifier.class );

    private static final String PADDING  = 
       "                                                                "; 

    //----------------------------------------------------------
    // state
    //----------------------------------------------------------

    private final InitialContext m_context;

    private final String m_root;

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

   /**
    * Creation of a new repository verifier.
    * @param context the repository inital context
    * @exception Exception if an error occurs
    */
    public RepositoryVerifier( 
      InitialContext context ) throws Exception
    {
        m_context = context;
        m_root = m_context.getInitialCacheDirectory().toString();
    }

    void verify()
    {
        StringBuffer buffer = 
          new StringBuffer( InitialContext.LINE );
        buffer.append( "\nAvalon Repository" );
        buffer.append( InitialContext.LINE );

        prepareInfoListing( buffer );
        buffer.append( InitialContext.LINE );

        prepareContentListing( buffer );
        buffer.append( InitialContext.LINE );

        System.out.println( buffer.toString() );
    }

    private void prepareInfoListing( StringBuffer buffer )
    {
        buffer.append( "\n${avalon.repository.cache} = " );
        buffer.append( m_context.getInitialCacheDirectory() );
        buffer.append( "\n${avalon.dir} = " );
        buffer.append( m_context.getInitialWorkingDirectory() );
        String[] hosts = m_context.getInitialHosts();
        buffer.append( "\n${avalon.repository.hosts} = (" );
        buffer.append( hosts.length );
        buffer.append( ")" );
        for( int i=0; i<hosts.length; i++ )
        {
            buffer.append( "\n  " + hosts[i] );
        }
    }

    private void prepareContentListing( StringBuffer buffer )
    {
        File cache = m_context.getInitialCacheDirectory();
        File[] groups = getGroups( cache );
        int n = getGroupsWidth( groups );
        for( int i=0; i<groups.length; i++ )
        {
            prepareGroupListing( buffer, groups[i], n );
        }
    }

    private int getGroupsWidth( File[] groups )
    {
        int n = 0;
        for( int i=0; i<groups.length; i++ )
        {
            File group = groups[i];
            int j = group.toString().length();
            if( j > n ) n = j;
        }
        return n;
    }

    private void prepareGroupListing( StringBuffer buffer, File file, int n )
    {

        int rootLength = m_root.length() + 1;
        String path = file.toString();
        String group = path.substring( rootLength );

        int offset = n - rootLength + 3;
        int padding = offset - group.length();
        String pad = PADDING.substring( 0, padding );

        buffer.append( "\n" );
        buffer.append( "  " + group + pad );
        prepareTypeSummary( buffer, file );
    }

   /**
    * List the types within the group.
    * @param buffer the string buffer
    * @param file the group directory
    */
    private void prepareTypeSummary( StringBuffer buffer, File file )
    {
        File[] types = file.listFiles( new TypesFilter() );
        for( int i=0; i<types.length; i++ )
        {
            File type = types[i];
            String key = type.getName();
            File[] versions = type.listFiles( new VersionedArtifactFilter( key ) );
            if( i > 0 )
            {
                buffer.append( ", " );
            }
            else
            {
                buffer.append( " " );
            }
            buffer.append( 
              type.getName() 
              + ":" 
              + versions.length );
        }
        /*
        for( int i=0; i<types.length; i++ )
        {
            File type = types[i];
            String key = type.getName();
            File[] versions = type.listFiles( new VersionedArtifactFilter( key ) );
            for( int j=0; j<versions.length; j++ )
            {
                File artifact = versions[j];
                String name = artifact.getName();
                buffer.append( "\n    " + name );
            }            
        }
        */
    }

   /**
    * Return the parent of the last directory.
    * @return the groups
    */
    private File[] getGroups( File root )
    {
        return root.listFiles( new DirectoryFilter() );
    }

    private class TypesFilter implements FileFilter
    {
        public boolean accept( File file )
        {
            if( !file.isDirectory() ) return false;
            final String type = file.getName();
            File[] artifacts = file.listFiles( new ArtifactFilter( type ) );
            return artifacts.length > 0;
        }
    }

    private class DirectoryFilter implements FileFilter
    {
        public boolean accept( File file )
        {
            return file.isDirectory();
        }
    }

    private class VersionedArtifactFilter extends ArtifactFilter
    {
        public VersionedArtifactFilter( String type )
        {
            super( type );
        }
        public boolean accept( File file )
        {
            return super.accept( file );
        }
    }

    private class ArtifactFilter implements FileFilter
    {
        private String m_type;
        public ArtifactFilter( String type )
        {
            int n = type.length();
            m_type = type.substring( 0, n-1 );
        }
        public boolean accept( File file )
        {
            if( file.isDirectory() ) return false;
           
            return file.getName().endsWith( m_type );
        }
    }
}
