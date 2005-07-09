/*
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

package net.dpml.magic.bar;

import net.dpml.magic.tasks.ProjectTask;
import net.dpml.transit.artifact.Handler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.PatternSet;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.jar.Manifest;

/**
 * Unpack a bar file into a repository.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class UnbarTask extends ProjectTask
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
    private URI m_uri;

    public void setCache( final File cache )
    {
        m_cache = cache;
    }

    public void setUri( final URI uri )
    {
        if( m_bar != null )
        {
            final String error =
              "The uri and file and attributes are mutually exclusive.";
            throw new BuildException( error );
        }

        m_uri = uri;
    }

    public void setFile( final File bar )
    {
        if( m_uri != null )
        {
            final String error =
              "The uri and file attributes are mutually exclusive.";
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

    private File getBar()
    {
        if( null != m_bar )
        {
            return m_bar;
        }
        else if( null != m_uri )
        {
            try
            {
                URL url = new URL( null, m_uri.toString(), new Handler() );
                return (File) url.getContent( new Class[]{ File.class } );
            }
            catch( Throwable e )
            {
                final String error =
                  "Unable to resolve the supplied uri ["
                  + m_uri
                  + "] to a file.";
                throw new BuildException( error );
            }
        }
        else
        {
            final String error =
              "You must declare a uri, file or href attribute.";
            throw new BuildException( error );
        }
    }

    private File getCache()
    {
        if( null != m_cache )
        {
            return m_cache;
        }
        else
        {
           return getCacheDirectory();
        }
    }

    public void execute() throws BuildException
    {
        File bar = getBar();
        File cache = getCache();

        log( "bar: " + bar );
        log( "cache: " + cache );

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
        return manifest.getAttributes( BLOCK ).getValue( key );
    }
}
