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

package org.apache.avalon.repository.main ;

import java.io.File;
import java.net.URL;
import java.util.Map;

import junit.framework.TestCase ;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;

/**
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultInitialContextTest.java 46177 2004-09-16 13:33:18Z niclas $
 */
public class DefaultInitialContextTest extends TestCase
{
    private static final String KEY = "merlin";

    private static final File BASEDIR = getWorkDir();

    private static File getWorkDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            throw new IllegalStateException( "Missing 'project.dir' property." );
        }
    }

    /**
     * Constructor for DefaultInitialContextTest.
     * @param name the test name
     */
    public DefaultInitialContextTest( String name )
    {
        super( name );
    }

    public void testRepositoryBootstrap() throws Exception
    {
        DefaultInitialContextFactory factory = 
          new DefaultInitialContextFactory( KEY, BASEDIR );
        factory.setCacheDirectory( getRepositoryDirectory() );
        factory.setHosts( getDefaultHosts() );

        InitialContext context = factory.createInitialContext();

        assertEquals( 
          "cache", 
          context.getInitialCacheDirectory(), 
          getRepositoryDirectory() );

        String[] defaults = getDefaultHosts();
        String[] hosts = context.getInitialHosts();
        assertNotNull( "hosts", hosts );
        assertEquals( "hosts count", defaults.length, hosts.length );

        for( int i=0; i<defaults.length; i++ )
        {
            assertEquals( 
              "host", defaults[i], hosts[i] );
        }

        Factory initialFactory = context.getInitialFactory();
        assertNotNull( initialFactory );

        Repository repository = (Repository) context.getRepository() ;
        assertNotNull( repository ) ;
   
        Artifact artifact = Artifact.createArtifact( "@REPO_API_SPEC@" );
        URL url = repository.getResource( artifact );
        assertNotNull( "url", url );
    }

    private static File getRepositoryDirectory()
    {
        return new File( System.getProperty( "project.repository.cache.path" ) );
    }

    private static String[] getDefaultHosts()
    {
        return new String[0];
    }
}
