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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.PatternSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.Manifest;

/**
 * Unpack a bar file into a repository. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class UnbarTask extends Task
{
   /**
    * Group identifier manifest key.
    */
    public static final String BLOCK_KEY_KEY = "Block-Key";
    public static final String BLOCK_GROUP_KEY = "Block-Group";
    public static final String BLOCK_NAME_KEY = "Block-Name";
    public static final String BLOCK_VERSION_KEY = "Block-Version";
    public static final String BLOCK = "Block";

    public static final String BAR_EXT = "bar";

    private File m_cache;
    private File m_bar;
    private URL m_href;

    public void setRepository( final File cache )
    {
        m_cache = cache;
    }

    public void setHref( final URL href )
    {
        if( m_bar == null )
        {
            m_href = href;
        }
        else
        {
            final String error = 
              "The file and href attributes are mutually exclusive.";
            throw new BuildException( error );
        }
    }
    
    public void setFile( final File bar )
    {
        if( m_href != null )
        {
            final String error = 
              "The file and href attributes are mutually exclusive.";
            throw new BuildException( error );
        }

        if( bar.exists() )
        {
            m_bar = bar;
        }
        else
        {
            final String error = 
              "Bar file not found: " + bar;
            throw new BuildException( error );
        }
    }
    
    private File getRepository()
    {
        if( null == m_cache )
        {
            return getDefaultRepository();
        }
        else
        {
            return m_cache;
        }
    }

    private File getBar()
    {
        if( null != m_bar )
        {
            return m_bar;
        }
        else if( null != m_href )
        {
            return getRemoteBar( m_href );
        }
        else
        {
            final String error = 
              "You must declare the file or href attribute.";
            throw new BuildException( error );
        }
    }

    private File getRemoteBar( URL url )
    {
        File temp = createTempFile();
        Get get = (Get) getProject().createTask( "get" );
        get.setSrc( url );
        get.setDest( temp );
        get.init();
        get.execute();
        return temp;
    }

    private File createTempFile()
    {
        try
        {
            File temp = File.createTempFile( "~magic-", ".bar" );
            temp.deleteOnExit();
            return temp;
        }
        catch( IOException ioe )
        {
            throw new BuildException( ioe );
        }
    }



    private File getDefaultRepository()
    {
        String cache = getProject().getProperty( "magic.cache" );
        if( null != cache )
        {
            return new File( cache );
        }
        else
        {
            final String error = 
              "Unbar task currently restricted to usage within magic build files.";
            throw new BuildException( error );
        }
    }

    public void execute() throws BuildException 
    {
        File bar = getBar();
        File cache = getRepository();

        log( "bar: " + bar );
        log( "cache: " + getRepository() );

        try
        {
            
            URL jurl = new URL( "jar:" + bar.toURL() + "!/" );
            JarURLConnection connection = (JarURLConnection) jurl.openConnection();
            Manifest manifest = connection.getManifest();
            final String key = getBlockAttribute( manifest, BLOCK_KEY_KEY );
            final String group = getBlockAttribute( manifest, BLOCK_GROUP_KEY );
            final String name = getBlockAttribute( manifest, BLOCK_NAME_KEY );
            final String version = getBlockAttribute( manifest, BLOCK_VERSION_KEY );

            log( "key: " + key );
            log( "group: " + group );
            log( "name: " + name );
            if( null != version )
            {
                log( "version: " + version );
            }

            File destination = new File( cache, group );
            Expand expand = (Expand) getProject().createTask( "unjar" );
            expand.setSrc( bar );
            expand.setDest( destination );
            PatternSet patternset = new PatternSet();
            patternset.createInclude().setName( "**/*" );
            patternset.createExclude().setName( "META-INF/**" );
            expand.addPatternset( patternset );
            expand.setTaskName( getTaskName() );
            expand.init();
            expand.execute(); 
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private String getBlockAttribute( Manifest manifest, String key )
    {
        return (String) manifest.getAttributes( BLOCK ).getValue( key );
    }
}
