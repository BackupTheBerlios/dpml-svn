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

package org.apache.avalon.tools.model;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TimeZone;

/**
 * A context contains infomation about a particular project build environment 
 * including the mapping of inital structure to the immutable magic structure.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Context extends Task
{
   /**
    * The reference name of the context object withing the current 
    * ant project.
    */
    public static final String KEY = "project.context";

   /**
    * The property name of the project key.
    */
    public static final String PROJECT_KEY = "project.key";


   /**
    * The value of the immutable target directory name.
    */
    public static final String TARGET = "target";

   /**
    * The value of the immutable target build directory name.
    */
    public static final String BUILD = "build";

   /**
    * The value of the immutable target classes directory name.
    */
    public static final String CLASSES = "classes";

   /**
    * The value of the immutable target deliverables directory name.
    */
    public static final String DELIVERABLES = "deliverables";

   /**
    * The value of the immutable target test classes directory name.
    */
    public static final String TEST_CLASSES = "test-classes";

   /**
    * The value of the immutable target test reports directory name.
    */
    public static final String TEST_REPORTS = "test-reports";

   /**
    * The value of the immutable target temp directory name.
    */
    public static final String TEMP = "temp";

   /**
    * The value of the immutable target test directory name.
    */
    public static final String TEST = "test";

   /**
    * The value of the immutable target docs directory name.
    */
    public static final String DOCS = "docs";

   /**
    * The value of the immutable user.properties filename.
    */
    private static final String USER_PROPERTIES = "user.properties";

   /**
    * The value of the immutable build.properties filename.
    */
    private static final String BUILD_PROPERTIES = "build.properties";

   /**
    * The value of the immutable project.src property key.
    */
    public static final String SRC_KEY = "project.src";

   /**
    * The value of the default project.src property value.
    */
    public static final String SRC_VALUE = "src";

   /**
    * The value of the default project.src.main property value.
    */
    public static final String SRC_MAIN = "main";

   /**
    * The value of the immutable project.src.main key.
    */
    public static final String SRC_MAIN_KEY = "project.src.main";

   /**
    * The value of the immutable project.src.config key.
    */
    public static final String SRC_CONFIG_KEY = "project.src.conf";

   /**
    * The value of the default project.src.cofig property value.
    */
    public static final String SRC_CONFIG = "conf";

    public static final String SRC_TEST_KEY = "project.src.test";

    /**
    * the key for the include pattern for test cases
    */
    public static final String TEST_INCLUDES_KEY = "project.test.includes";
    
    /**
    * default value
    */
    public static final String TEST_INCLUDES_VALUE = "**/*TestCase.java, **/*Test.java";
    
    /**
    * the key for the exclude pattern for test cases
    */
    public static final String TEST_EXCLUDES_KEY = "project.test.excludes";

   /**
    * default value
    */
    public static final String TEST_EXCLUDES_VALUE = "**/Abstract*.java, **/AllTest*.java";
    
    /**
    * The value of the default project.src.test property value.
    */
    public static final String SRC_TEST = "test";

   /**
    * The value of the immutable project.etc key.
    */
    public static final String ETC_KEY = "project.etc";

   /**
    * The value of the default project.etc default value.
    */
    public static final String ETC_VALUE = "etc";

   /**
    * Reuturn the context object for the project.  
    * If the project does not have a reference assigned to 
    * the name 'project.context' then a new context is created and 
    * assigned.
    *
    * @param project the current project
    * @return the bound context
    */
    public static Context getContext( final Project project )
    {
        if( null == project )
        {
            throw new NullPointerException( "project" );
        }

        Context context = (Context) project.getReference( KEY );
        if( null == context )
        {
            context = new Context();
            context.setProject( project );
            context.init();
            context.execute();
            project.addReference( KEY, context );
        }
        return context;
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
    */ 
    public static File getFile( final File root, final String path, boolean create )
    {
        if( null == path )
        {
            throw new NullPointerException( "path" );
        }
        final File file = new File( path );
        if( file.isAbsolute() ) return getCanonicalFile( file, create );
        if( null == root )
        {
            throw new NullPointerException( "root" );
        }
        return getCanonicalFile( new File( root, path ), create );
    }

   /**
    * Return the concatonal variant of a file.
    * @param file the file argument
    * @return the concatonal variant
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
    * @return the concatonal variant
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
    * @return the concatonal path
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

   /**
    * Return the UTC YYMMDD.HHMMSSS signature.
    * @return the UTC date-stamp
    */
    public static String getSignature()
    {
        return getSignature( new Date() );
    }

   /**
    * Return the UTC YYMMDD.HHMMSSS signature of a date.
    * @param date the date
    * @return the UTC date-stamp signature
    */
    public static String getSignature( final Date date )
    {
        final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return sdf.format( date );
    }

    private final Map m_map = new Hashtable();

    private String m_key;

    private File m_src;
    private File m_etc;

    private File m_target;
    private File m_build;
    private File m_deliverables;
    private File m_classes;
    private File m_testClasses;
    private File m_testReports;
    private File m_temp;
    private File m_docs;
    private File m_test;
    private String m_testIncludes;
    private String m_testExcludes;

   /**
    * Creation of the context and association ofnthe context under the KEY key.
    */
    public void execute()
    {
        final Project project = getProject();
        setupProperties( project );

        m_key = resolveKey();

        project.setNewProperty( PROJECT_KEY, getKey() );

        project.setNewProperty( SRC_KEY, SRC_VALUE );
        project.setNewProperty( SRC_MAIN_KEY, SRC_MAIN );
        project.setNewProperty( SRC_CONFIG_KEY, SRC_CONFIG );
        project.setNewProperty( SRC_TEST_KEY, SRC_TEST );
        project.setNewProperty( TEST_INCLUDES_KEY, TEST_INCLUDES_VALUE );
        project.setNewProperty( TEST_EXCLUDES_KEY, TEST_EXCLUDES_VALUE );

        project.setNewProperty( ETC_KEY, ETC_VALUE );

        final File basedir = project.getBaseDir();
        final String src = project.getProperty( SRC_KEY );
        final String etc = project.getProperty( ETC_KEY );

        m_src = setupSrc( basedir, src );
        m_etc = setupEtc( basedir, etc );

        m_target = new File( basedir, TARGET );
        m_build = setBuildPath( BUILD );
        m_deliverables = setBuildPath( DELIVERABLES );
        m_classes = setBuildPath( CLASSES );
        m_temp = setBuildPath( TEMP );
        m_test = setBuildPath( TEST );
        m_testClasses = setBuildPath( TEST_CLASSES );
        m_testReports = setBuildPath( TEST_REPORTS );
        m_docs = setBuildPath( DOCS );
        m_testIncludes = setupTestIncludes(project.getProperty(TEST_INCLUDES_KEY));
        m_testExcludes = setupTestExcludes(project.getProperty(TEST_EXCLUDES_KEY));
        project.addReference( KEY, this );

    }

   /**
    * Return the unique key for this project.
    * @return the project key
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Reuturn the project src directory.
    * @return the src directory
    */
    public File getSrcDirectory()
    {
        return m_src;
    }

   /**
    * Reuturn the project etc directory.
    * @return the etc directory
    */
    public File getEtcDirectory()
    {
        return m_etc;
    }

   /**
    * Reuturn the project target directory.
    * @return the target directory
    */
    public File getTargetDirectory()
    {
        return m_target;
    }

   /**
    * Reuturn the project target build directory.
    * @return the target build directory
    */
    public File getBuildDirectory()
    {
        return m_build;
    }

   /**
    * Reuturn the project deliverables directory.
    * @return the target deliverables directory
    */
    public File getDeliverablesDirectory()
    {
        return m_deliverables;
    }

   /**
    * Reuturn the project classes directory.
    * @return the target classes directory
    */
    public File getClassesDirectory()
    {
        return m_classes;
    }

   /**
    * Reuturn the project test classes directory.
    * @return the target test classes directory
    */
    public File getTestClassesDirectory()
    {
        return m_testClasses;
    }

   /**
    * Reuturn the project test reports directory.
    * @return the target test reports directory
    */
    public File getTestReportsDirectory()
    {
        return m_testReports;
    }

   /**
    * Reuturn the project temp directory.
    * @return the target temp directory
    */
    public File getTempDirectory()
    {
        return m_temp;
    }

   /**
    * Reuturn the project test directory.
    * @return the target test directory
    */
    public File getTestDirectory()
    {
        return m_test;
    }
    
    /**
    * Return the project test includes.
    * @return the includes pattern
    */
    public String getTestIncludes()
    {
        return m_testIncludes;
    }
    
    /**
    * Return the project test excludes.
    * @return the excludes pattern
    */
    public String getTestExcludes()
    {
        return m_testExcludes;
    }

   /**
    * Reuturn the project docs directory.
    * @return the target docs directory
    */
    public File getDocsDirectory()
    {
        return m_docs;
    }

   /**
    * Reserve a path under the target directory.
    * @param path the path to reserve
    */
    public File setBuildPath( final String path )
    {
        return setBuildPath( path, path );
    }

   /**
    * Reserve a path under the target directory.
    * @param key the logic path identifier
    * @param path the path to reserve
    */
    public File setBuildPath( final String key, final String path )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == path )
        {
            throw new NullPointerException( "null path for key: " + key );
        }
        if( m_map.containsKey( key ) )
        {
            final String error = 
              "Duplicate path registration request for key '" 
              + key + "'.";
            throw new BuildException( error );
        }
        final File build = getFile( m_target, path );
        m_map.put( key, build );
        return build;
    }

   /**
    * Return a build path matching a supplied key.
    * @return the target path
    */
    public File getBuildPath( final String key )
    {
        if( m_map.containsKey( key ) )
        {
            return (File) m_map.get( key ) ;
        }
        
        final String error = 
          "Unknown build key '" 
          + key + "'.";
        throw new BuildException( error );
    }

    //--------------------------------------------------------------------
    // internal
    //--------------------------------------------------------------------

    private String resolveKey()
    {
        if( null != m_key )
        {
            return m_key;
        }
        else
        {
            final String key = getProject().getProperty( "project.key" );
            if( null != key )
            {
                m_key = key;
                return key;            
            }
            else
            {
                final String name = getProject().getProperty( "project.name" );
                if( null != name )
                {
                    m_key = name;
                    return name;            
                }
                else
                {
                    m_key = getProject().getName();
                    return m_key;
                }
            }
        }
    }

    private static File setupSrc( final File basedir, final String path )
    {
        if( null == path ) return new File( basedir, SRC_VALUE );
        return new File( basedir, path );
    }
    
    private static String setupTestIncludes( final String includes )
    {
        if( null == includes ) 
            return TEST_INCLUDES_VALUE;
        return includes;
    }
    
    private static String setupTestExcludes( final String excludes )
    {
        if( null == excludes ) 
            return TEST_EXCLUDES_VALUE;
        return excludes;
    }


    private static File setupEtc( final File basedir, final String path )
    {
        if( null == path ) 
            return new File( basedir, ETC_VALUE );
        return new File( basedir, path );
    }

    private void setupProperties( final Project project )
    {
        final File basedir = project.getBaseDir();
        setupUserProperties( project, basedir );
        setupBuildProperties( project, basedir );
    }

    private void setupUserProperties( final Project project, final File basedir )
    {
        final File user = Context.getFile( basedir, USER_PROPERTIES );
        readProperties( project, user );
    }

    private void setupBuildProperties( final Project project, final File basedir )
    {
        final File build = Context.getFile( basedir, BUILD_PROPERTIES );
        readProperties( project, build );
    }

    private void readProperties( final Project project, final File file ) throws BuildException
    {
        final Property props = (Property) project.createTask( "property" );
        props.setFile( file );
        props.init();
        props.execute();
    }

}
