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

package org.apache.avalon.merlin.cli.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;

import org.apache.avalon.merlin.cli.Main;

/**
 * Test case for the Merlin CLI handler.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MerlinCLITestCase.java 46177 2004-09-16 13:33:18Z niclas $
 */
public class MerlinCLITestCase extends TestCase
{
    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

    /**
     * Constructor for MerlinCLITestCaseCase.
     * @param name the name of the testcase
     */
    public MerlinCLITestCase( String name )
    {
        super( name );
    }

    //----------------------------------------------------------
    // testcase
    //----------------------------------------------------------

    public void testMain() throws Exception
    {
        String system = getCacheDirectory().toString();
        String basedir = System.getProperty( "project.dir" );

        try
        {
            String[] args = 
              new String[]
              {
                "-execute",
                "-repository",
                system,
                "-home",
                basedir,
                "-system",
                system,
                "-offline"
              };
            Main.main( args );
        }
        catch( Throwable e )
        {
            final String error = ExceptionHelper.packException( e, false );
            fail( error );
        }
    }

    private File getCacheDirectory()
    {
        String cache = System.getProperty( "project.repository.cache.path" );
        if( null != cache )
        {
            return new File( cache );
        }
        else
        {
            return getMavenRepositoryDirectory();
        }
    }

    private static File getMavenRepositoryDirectory()
    {
        return new File( getMavenHomeDirectory(), "repository" );
    }

    private static File getMavenHomeDirectory()
    {
        return new File( getMavenHome() );
    }

    private static String getMavenHome()
    {
        try
        {
            String local = 
              System.getProperty( 
                "maven.home.local", 
                Env.getEnvVariable( "MAVEN_HOME_LOCAL" ) );
            if( null != local ) return local;

            return System.getProperty( "user.home" ) + File.separator + ".maven";

        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }
}
