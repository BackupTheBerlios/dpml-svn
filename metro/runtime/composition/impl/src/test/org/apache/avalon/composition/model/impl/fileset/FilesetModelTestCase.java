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

package org.apache.avalon.composition.model.impl.fileset;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.avalon.composition.data.FilesetDirective;
import org.apache.avalon.composition.data.IncludeDirective;
import org.apache.avalon.composition.data.ExcludeDirective;
import org.apache.avalon.composition.model.impl.DefaultFilesetModel;
import org.apache.avalon.framework.logger.ConsoleLogger;

public class FilesetModelTestCase extends TestCase
{
    private static File getWorkDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            path = System.getProperty( "basedir" );
            File root = new File( path );
            return new File( root, "target/test-classes" );
        }
    }

    private ConsoleLogger m_logger;
    private File m_root;

    /**
     * Sets up the test case.
     */
    public void setUp()
    {
        m_root = getWorkDir();
        m_logger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );
    }


    /**
     * Cleans up the test case.
     */
    public void tearDown() throws Exception
    {
        m_logger = null;
    }

    /**
     * A fileset directive of:
     *
     *      <fileset dir="my-dir">
     *          ...
     *      </fileset>
     *
     * should throw an IllegalStateException.
     */
    public void testBadBaseDirectory() throws Exception
    {
        // only testing a bad base directory -- no includes or excludes necessary
        IncludeDirective[] includes = new IncludeDirective[0];
        ExcludeDirective[] excludes = new ExcludeDirective[0];

        // make up a *bad* fileset directory attribute
        FilesetDirective fsd = new FilesetDirective( "junk", includes, excludes );

        // create the fileset model's anchor directory
        File anchor = new File( m_root, fsd.getBaseDirectory() );

        // create the Fileset Model
        DefaultFilesetModel m_model = new DefaultFilesetModel( anchor, fsd.getIncludes(),
            fsd.getExcludes(), null, null, m_logger );

        // do the test...
        try
        {
        	m_model.resolveFileset();
            fail("The test did not fail with an IllegalStateException");
        }
        catch( IllegalStateException ise )
        {
            // success
        }
        catch( IOException ioe )
        {
            fail("The exception thrown was an " + ioe.getClass().getName() );
        }
        catch( Exception e )
        {
            fail("The exception thrown was " + e.getClass().getName() );
        }
    }

    /**
     * A fileset directive of:
     *
     *      <fileset dir="my-dir"/>
     *
     * should return an include set of a single entry that is
     * the fully qualified path to the my-dir directory.
     */
    public void testZeroIncludesExcludes() throws Exception
    {
        // testing empty include/exclude directives
        IncludeDirective[] includes = new IncludeDirective[0];
        ExcludeDirective[] excludes = new ExcludeDirective[0];

        // provide legitimate fileset directory attribute
        final String dir = ".";
        FilesetDirective fsd = new FilesetDirective( dir, includes, excludes );

        // create the fileset model's anchor directory
        File anchor = new File( m_root, fsd.getBaseDirectory() );

        // create the Fileset Model
        DefaultFilesetModel m_model = new DefaultFilesetModel( anchor, fsd.getIncludes(),
            fsd.getExcludes(), null, null, m_logger );

        // do the test...
        try
        {
        	m_model.resolveFileset();
            ArrayList list = m_model.getIncludes();
            if ( list.size() != 1 )
            {
                fail( "The include set returned did not equal 1 (one) entry" );
            }
            File file = (File) list.get( 0 );
            if ( !file.isDirectory() )
            {
                fail( "The included entry is not a directory" );
            }
            if ( !file.equals( anchor ) )
            {
                fail( "The included directory entry does not match the fileset directory attribute" );
            }
        }
        catch( IllegalStateException ise )
        {
            fail( "The exception thrown was an " + ise.getClass().getName() );
        }
        catch( IOException ioe )
        {
            fail( "The exception thrown was an " + ioe.getClass().getName() );
        }
        catch( Exception e )
        {
            fail( "The exception thrown was " + e.getClass().getName() );
        }
    }
    
    /**
     * A fileset directive of:
     *
     *      <fileset dir="my-dir">
     *          <include name="*.jar/>
     *      </fileset>
     *
     * should return all the files in my-dir with the
     * .jar extension.
     */
    public void testWildcardIncludes() throws Exception
    {
        // testing an include directive = "*.widget"
        IncludeDirective[] includes = new IncludeDirective[1];
        includes[0] = new IncludeDirective( "*.widget" );

        // testing empty exclude directives
        ExcludeDirective[] excludes = new ExcludeDirective[0];

        // provide legitimate fileset directory attribute
        final String dir = "fileset";        
        FilesetDirective fsd = new FilesetDirective( dir, includes, excludes );

        // create the fileset model's anchor directory
        File anchor = new File( m_root, fsd.getBaseDirectory() );

        // create the Fileset Model
        DefaultFilesetModel m_model = new DefaultFilesetModel( anchor, fsd.getIncludes(),
            fsd.getExcludes(), null, null, m_logger );

        // do the test...
        try
        {
        	m_model.resolveFileset();
            ArrayList list = m_model.getIncludes();
            if ( list.size() != 4 )
            {
                fail( 
                  "The include set returned [" + list.size() 
                  + "] entries which does not equal 4 (four) expected entries" );
            }
            for ( int i = 0; i < list.size(); i++ )
            {
                File file = (File) list.get( i );
                if ( !file.isFile() )
                {
                    fail( "One of the included entries is not a file" );
                }
                if ( !file.getName().endsWith( "widget" ) )
                {
                    fail( "One of the included file entries does not have a .widget extension" );
                }
            }
        }
        catch( IllegalStateException ise )
        {
            fail( "The exception thrown was an " + ise.getClass().getName() );
        }
        catch( IOException ioe )
        {
            fail( "The exception thrown was an " + ioe.getClass().getName() );
        }
        catch( Exception e )
        {
            fail( "The exception thrown was " + e.getClass().getName() );
        }
    }

    /**
     * A fileset directive of:
     *
     *      <fileset dir="my-dir">
     *          <include name="*.jar/>
     *          <exclude name="test*.jar/>
     *      </fileset>
     *
     * should return all the files in my-dir with the
     * .jar extension with the exception of jar files
     * whose filename begins with "test".
     */
    public void testIncludeExcludes() throws Exception
    {
        // testing an include directive = "*.widget"
        IncludeDirective[] includes = new IncludeDirective[1];
        includes[0] = new IncludeDirective( "*.widget" );

        // testing an exclude directive = "test*.widget"
        ExcludeDirective[] excludes = new ExcludeDirective[1];
        excludes[0] = new ExcludeDirective( "test*.widget" );

        // provide legitimate fileset directory attribute
        final String dir = "fileset";
        FilesetDirective fsd = new FilesetDirective( dir, includes, excludes );

        // create the fileset model's anchor directory
        File anchor = new File( m_root, fsd.getBaseDirectory() );

        // create the Fileset Model
        DefaultFilesetModel m_model = new DefaultFilesetModel( anchor, fsd.getIncludes(),
            fsd.getExcludes(), null, null, m_logger );

        // do the test...
        try
        {
        	m_model.resolveFileset();
            ArrayList list = m_model.getIncludes();
            if ( list.size() != 2 )
            {
                fail( "The include set returned did not equal 2 (two) entries" );
            }
            for ( int i = 0; i < list.size(); i++ )
            {
                File file = (File) list.get( i );
                if ( !file.isFile() )
                {
                    fail( "One of the included entries is not a file" );
                }
                if ( !file.getName().endsWith( "widget" ) )
                {
                    fail( "One of the included file entries does not have a .widget extension" );
                }
                if ( file.getName().startsWith( "test" ) )
                {
                    fail( "One of the include file entries has a filename beginning with 'test'" );
                }
            }
        }
        catch( IllegalStateException ise )
        {
            fail( "The exception thrown was an " + ise.getClass().getName() );
        }
        catch( IOException ioe )
        {
            fail( "The exception thrown was an " + ioe.getClass().getName() );
        }
        catch( Exception e )
        {
            fail( "The exception thrown was " + e.getClass().getName() );
        }
    }

    /**
     * A fileset directive of:
     *
     *      <fileset dir="my-dir">
     *          <include name="**//*.jar/>
     *      </fileset>
     *
     * should return all the files in my-dir and it's child
     * directories with the .jar extension.
     */
    public void testRecursiveIncludes() throws Exception
    {
        // testing an include directive = "**/*.widget"
        IncludeDirective[] includes = new IncludeDirective[1];
        includes[0] = new IncludeDirective( "**/*.widget" );

        // testing empty exclude directives
        ExcludeDirective[] excludes = new ExcludeDirective[0];

        // provide legitimate fileset directory attribute
        final String dir = "fileset";
        FilesetDirective fsd = new FilesetDirective( dir, includes, excludes );

        // create the fileset model's anchor directory
        File anchor = new File( m_root, fsd.getBaseDirectory() );

        // create the Fileset Model
        DefaultFilesetModel m_model = new DefaultFilesetModel( anchor, fsd.getIncludes(),
            fsd.getExcludes(), null, null, m_logger );

        // do the test...
        try
        {
        	m_model.resolveFileset();
            ArrayList list = m_model.getIncludes();
            if ( list.size() != 9 )
            {
                fail( "The include set returned did not equal 9 (nine) entries" );
            }
            for ( int i = 0; i < list.size(); i++ )
            {
                File file = (File) list.get( i );
                if ( !file.isFile() )
                {
                    fail( "One of the included entries is not a file" );
                }
                if ( !file.getName().endsWith( "widget" ) )
                {
                    fail( "One of the included file entries does not have a .widget extension" );
                }
            }
        }
        catch( IllegalStateException ise )
        {
            fail( "The exception thrown was an " + ise.getClass().getName() );
        }
        catch( IOException ioe )
        {
            fail( "The exception thrown was an " + ioe.getClass().getName() );
        }
        catch( Exception e )
        {
            fail( "The exception thrown was " + e.getClass().getName() );
        }
    }
}
