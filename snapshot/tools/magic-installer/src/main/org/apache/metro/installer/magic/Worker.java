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

package org.apache.metro.installer.magic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class Worker
{
    private static final int BUFFER_SIZE = 10000;
    
    private File m_MagicHome;
    private File m_AntLibDir;
    private File m_DevDir;
    
    private ProgressIndicator   m_Progress;
    private long                m_ProgressSize;    
    
    public Worker( ProgressIndicator indicator, 
                   File magicHome, File antLib, File devDir )
    {
        m_MagicHome = magicHome;
        m_AntLibDir = antLib;
        m_Progress = indicator;
        m_DevDir = devDir;
    }
    
    public void start()
        throws Exception
    {
        /* Not sure what to do with these yet.
        File md5File = download( "http://www.dpml.net/avalon/tools/bars/avalon-tools-magic.bar.md5" );
        File ascFile = download( "http://www.dpml.net/avalon/tools/bars/avalon-tools-magic.bar.asc" );
        */
        
        String url = "http://www.dpml.net/avalon/tools/bars/avalon-tools-magic.bar";
        File barFile = download( url );
        unjar( barFile, m_MagicHome );
        
        File toolsJar = new File( m_MagicHome, "jars/avalon-tools-magic.jar" );
        copy( toolsJar, m_AntLibDir );
        
        File globalBuild = new File( m_MagicHome, "templates/global/build.xml" );
        
        // Below is for testing purposes. The following 2 lines should be removed
        // as soon as Magic contains the global/build.xml file.
        if( ! globalBuild.isFile() )
            globalBuild = new File( m_MagicHome, "templates/standard.xml" );
            
        copy( globalBuild, m_DevDir );
        
        m_Progress.message( "Cleaning up." );
        
        File jarsDir = toolsJar.getParentFile();
        deleteDir( jarsDir );
        File barsDir = new File( m_MagicHome, "bars/" );
        deleteDir( barsDir );        
    }
    
    private void unjar( File barFile, File toDir )
        throws IOException
    {
        m_Progress.message( "Unzipping " + barFile );
        m_Progress.start();
        JarFile jar = new JarFile( barFile );
        Enumeration entries = jar.entries();
        while( entries.hasMoreElements() )
        {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();
            File dest = new File( toDir, name );
            if( entry.isDirectory() )
            {
                dest.mkdirs();
            }
            else
            {
                InputStream in = jar.getInputStream( entry );
                FileOutputStream out = new FileOutputStream( dest );
                m_ProgressSize = entry.getSize();
                copy( in, out );
                in.close();
                out.close();
            }
        }
        m_Progress.finished();
    }
    
    private File download( String url )
        throws IOException
    {        
        URL download = new URL( url );
        m_Progress.message( "Connecting to " + download.getHost() );

        URLConnection conn = download.openConnection();
        conn.connect();
        m_ProgressSize = conn.getContentLength();
        
        m_Progress.message( "Downloading " + url );
        m_Progress.start();
        InputStream in = conn.getInputStream();
        
        File tmp = File.createTempFile( "magic", null );
        tmp.deleteOnExit();
        
        FileOutputStream out = new FileOutputStream( tmp );
        
        copy( in, out );
        in.close();
        out.close();
        m_Progress.finished();
        return tmp;   
    }
    
    
    private void copy( InputStream from, OutputStream to )
        throws IOException
    {
        BufferedOutputStream out = new BufferedOutputStream( to );
        BufferedInputStream in = new BufferedInputStream( from, BUFFER_SIZE );
        int bytesRead = 0;
        int counter = 0;
        do
        {
            byte[] data = new byte[ BUFFER_SIZE ];
            bytesRead = in.read( data, 0, BUFFER_SIZE );
            counter = counter + bytesRead;
            if( m_ProgressSize != 0 )
                m_Progress.progress( (int) ( ( counter * 100 ) / m_ProgressSize ) );
            if( bytesRead != -1 )
                out.write( data, 0, bytesRead );
        } while( bytesRead != -1 );
        out.flush();
    }
    
    private void copy( File file, File toDir )
        throws IOException
    {
        m_Progress.message( "Copying " + file + " to " + file );
        m_Progress.start();
        toDir.mkdirs();
        String name = file.getName();
        File destFile = new File( toDir, name );
        
        FileInputStream in = new FileInputStream( file );
        FileOutputStream out = new FileOutputStream( destFile );
        
        m_ProgressSize = file.length();
        copy( in, out );
        
        in.close();
        out.close();
        m_Progress.finished();
    }
    
    private void deleteDir( File dir )
    {
        File[] files = dir.listFiles();
        for( int i=0 ; i < files.length ; i++ )
            files[i].delete();
        dir.delete();
    }
}
