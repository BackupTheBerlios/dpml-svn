/*
 * Copyright 2004-2005 Stephen McConnell
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

package net.dpml.tools.tasks;

import java.io.File;
import java.util.StringTokenizer;

import net.dpml.tools.ant.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.taskdefs.optional.junit.BatchTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * Load a goal.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class JUnitTestTask extends GenericTask
{
   /**
    * Constant test enabled key.
    */
    public static final String TEST_ENABLED_KEY = "project.test.enabled";

   /**
    * Constant test src directory key.
    */
    public static final String TEST_SRC_KEY = "project.test.src";

   /**
    * Constant test env directory key.
    */
    public static final String TEST_ENV_KEY = "project.test.env";

   /**
    * Constant test debug key.
    */
    public static final String DEBUG_KEY = "project.test.debug";

   /**
    * Constant test fork key.
    */
    public static final String FORK_KEY = "project.test.fork";

   /**
    * Constant test halt-on-error key.
    */
    public static final String HALT_ON_ERROR_KEY = "project.test.halt-on-error";

   /**
    * Constant test fork mode key.
    */
    public static final String TEST_FORK_MODE_KEY = "project.test.fork.mode";

   /**
    * Constant test halt-on-failure key.
    */
    public static final String HALT_ON_FAILURE_KEY = "project.test.halt-on-failure";

   /**
    * Constant cache path key.
    */
    public static final String CACHE_PATH_KEY = "dpml.cache";

   /**
    * Constant work dir key.
    */
    public static final String WORK_DIR_KEY = "project.test.dir";

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

    private File m_source;
    private String m_classPathRef;
    private Path m_classPath;
    
   /**
    * Task initialization.
    * @exception BuildException if a build error occurs.
    */
    public void init() throws BuildException
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty( DEBUG_KEY, "" + DEBUG_VALUE );
            project.setNewProperty( FORK_KEY, "" + FORK_VALUE );
            project.setNewProperty( TEST_SRC_KEY, "" + TEST_SRC_VALUE );
            project.setNewProperty( TEST_ENV_KEY, "" + TEST_ENV_VALUE );
            project.setNewProperty( HALT_ON_ERROR_KEY, "" + HALT_ON_ERROR_VALUE );
            project.setNewProperty( HALT_ON_FAILURE_KEY, "" + HALT_ON_FAILURE_VALUE );
            getContext().init();
        }
    }

   /**
    * Set the id of the compilation classpath.
    * @param id the classpath reference
    */
    public void setClasspathRef( String id ) 
    {
        m_classPathRef = id;
    }

   /**
    * Set the classpath.
    * @param path the classpath
    */
    public void setClasspath( Path path ) 
    {
        m_classPath = path;
    }

   /**
    * Set the directory containing the unit test source files.
    * @param source the test source directory
    */
    public void setSrc( File source )
    {
        m_source = source;
    }
    
    private Path getClasspath()
    {
        if( null != m_classPath )
        {
            return m_classPath;
        }
        else if( null != m_classPathRef )
        {
            return (Path) getProject().getReference( m_classPathRef );
        }
        else
        {
            final String error = 
              "Missing classpathref or classpath argument.";
            throw new BuildException( error, getLocation() );
        }
    }

   /**
    * Task execution.
    * @exception BuildException if a build error occurs.
    */
    public void execute() throws BuildException
    {
        final Project project = getProject();
        final String enabled = project.getProperty( TEST_ENABLED_KEY );
        if( ( null != enabled ) && enabled.equals( "false" ) )
        {
            return;
        }
        Context context = getContext();
        final File src = context.getTargetBuildTestDirectory();
        if( src.exists() )
        {
            final File working = context.getTargetTestDirectory();
            final Path classpath = getClasspath();
            executeUnitTests( src, classpath, working );
        }
        final String error = project.getProperty( ERROR_KEY );
        if( null != error )
        {
            final String message =
                "One or more unit test errors occured.";
            if( getHaltOnErrorProperty() )
            {
                    fail( message );
            }
        }
        final String failure = project.getProperty( FAILURE_KEY );
        if( null != failure )
        {
            final String message =
                "One or more unit test failures occured.";
            if( getHaltOnFailureProperty() )
            {
                fail( message );
            }
        }
    }

    private void executeUnitTests( final File src, final Path classpath, File working )
    {
        final Project project = getProject();
        log( "Test classpath: " + classpath, Project.MSG_VERBOSE );

        final FileSet fileset = new FileSet();
        fileset.setDir( src );
        final String includes = getTestIncludes();
        final String excludes = getTestExcludes();
        createIncludes( fileset, includes );
        createExcludes( fileset, excludes );
        log( "Test filters: includes=" + includes + ", excludes=" + excludes, Project.MSG_VERBOSE );

        final JUnitTask junit = (JUnitTask) getProject().createTask( "junit" );
        junit.init();

        final JUnitTask.SummaryAttribute summary = new JUnitTask.SummaryAttribute();
        summary.setValue( "on" );
        junit.setPrintsummary( summary );
        junit.setShowOutput( true );
        junit.setTempdir( working );
        junit.setReloading( true );
        junit.setFiltertrace( true );
        junit.createClasspath().add( classpath );
        junit.setHaltonerror(
          getBooleanProperty(
            HALT_ON_ERROR_KEY, HALT_ON_ERROR_VALUE ) );
        junit.setHaltonfailure(
          getBooleanProperty(
            HALT_ON_FAILURE_KEY, HALT_ON_FAILURE_VALUE ) );

        final File reports = getContext().getTargetReportsTestDirectory();
        mkDir( reports );

        final BatchTest batch = junit.createBatchTest();
        batch.addFileSet( fileset );
        batch.setTodir( reports );

        final FormatterElement plainFormatter = new FormatterElement();
        final FormatterElement.TypeAttribute plain = new FormatterElement.TypeAttribute();
        plain.setValue( "plain" );
        plainFormatter.setType( plain );
        junit.addFormatter( plainFormatter );

        final FormatterElement xmlFormatter = new FormatterElement();
        final FormatterElement.TypeAttribute xml = new FormatterElement.TypeAttribute();
        xml.setValue( "xml" );
        xmlFormatter.setType( xml );
        junit.addFormatter( xmlFormatter );

        final Environment.Variable work = new Environment.Variable();
        work.setKey( WORK_DIR_KEY );
        work.setValue( working.toString() );
        junit.addConfiguredSysproperty( work );

        final Environment.Variable testBaseDir = new Environment.Variable();
        testBaseDir.setKey( "project.test.dir" );
        testBaseDir.setValue( working.toString() );
        junit.addConfiguredSysproperty( testBaseDir );

        final Environment.Variable basedir = new Environment.Variable();
        basedir.setKey( "basedir" );
        basedir.setValue( project.getBaseDir().toString() );
        junit.addConfiguredSysproperty( basedir );

        final Environment.Variable basedir2 = new Environment.Variable();
        basedir2.setKey( "project.basedir" );
        basedir2.setValue( project.getBaseDir().toString() );
        junit.addConfiguredSysproperty( basedir2 );

        final Environment.Variable cache = new Environment.Variable();
        cache.setKey( CACHE_PATH_KEY );
        cache.setValue( getCachePath() );
        junit.addConfiguredSysproperty( cache );

        final File policy = new File( working, "security.policy" );
        if( policy.exists() )
        {
            final Environment.Variable security = new Environment.Variable();
            security.setKey( "java.security.policy" );
            security.setValue( policy.toString() );
            junit.addConfiguredSysproperty( security );
        }

        junit.setErrorProperty( ERROR_KEY );
        junit.setFailureProperty( FAILURE_KEY );
        junit.setTaskName( getTaskName() );
        if( getForkProperty() )
        {
            junit.setFork( true );
            junit.setDir( project.getBaseDir() );
            JUnitTask.ForkMode mode = getForkMode();
            if( null == mode )
            {
                log( "Executing forked test." );
            }
            else
            {
                log( "Executing forked test with mode: '" + mode + "'." );
                junit.setForkMode( mode );
            }
        }
        else
        {
            log( "executing in local jvm" );
            JUnitTask.ForkMode mode = new JUnitTask.ForkMode( "once" );
            junit.setForkMode( mode );
            junit.setFork( false );
        }
        junit.execute();
    }

    private String getTestIncludes()
    {
        String includes = getProject().getProperty( TEST_INCLUDES_KEY );
        if( null != includes )
        {
            return includes;
        }
        else
        {
            return TEST_INCLUDES_VALUE;
        }
    }

    private String getTestExcludes()
    {
        String excludes = getProject().getProperty( TEST_EXCLUDES_KEY );
        if( null != excludes )
        {
            return excludes;
        }
        else
        {
            return TEST_EXCLUDES_VALUE;
        }
    }

    private void createIncludes( FileSet set, String pattern )
    {
        StringTokenizer tokenizer = new StringTokenizer( pattern, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String item = tokenizer.nextToken();
            set.createInclude().setName( item );
        }
    }

    private void createExcludes( FileSet set, String pattern )
    {
        StringTokenizer tokenizer = new StringTokenizer( pattern, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String item = tokenizer.nextToken();
            set.createExclude().setName( item );
        }
    }

    private String getCachePath()
    {
        final String value = getProject().getProperty( CACHE_PATH_KEY );
        if( null != value )
        {
            return value;
        }
        else
        {
            File cache = (File) getProject().getReference( "dpml.cache" );
            return cache.toString();
        }
    }

    private boolean getDebugProperty()
    {
        return getBooleanProperty( DEBUG_KEY, DEBUG_VALUE );
    }

    private boolean getHaltOnErrorProperty()
    {
        return getBooleanProperty( HALT_ON_ERROR_KEY, HALT_ON_ERROR_VALUE );
    }

    private boolean getHaltOnFailureProperty()
    {
        return getBooleanProperty( HALT_ON_FAILURE_KEY, HALT_ON_FAILURE_VALUE );
    }

    private boolean getForkProperty()
    {
        return getBooleanProperty( FORK_KEY, FORK_VALUE );
    }

    private JUnitTask.ForkMode getForkMode()
    {
        final String value = getProject().getProperty( TEST_FORK_MODE_KEY );
        if( null == value )
        {
            return null;
        }
        else
        {
            return new JUnitTask.ForkMode( value );
        }
    }

    private boolean getBooleanProperty( final String key, final boolean fallback )
    {
        final String value = getProject().getProperty( key );
        if( null == value )
        {
            return fallback;
        }
        else
        {
            return Project.toBoolean( value );
        }
    }

    private void fail( final String message )
    {
        final Exit exit = (Exit) getProject().createTask( "fail" );
        exit.setMessage( message );
        exit.init();
        exit.execute();
    }

    private static final String ERROR_KEY = "project.test.error";
    private static final String FAILURE_KEY = "project.test.failure";

    private static final String TEST_SRC_VALUE = "test";
    private static final String TEST_ENV_VALUE = "env";
    private static final boolean DEBUG_VALUE = true;
    private static final boolean FORK_VALUE = false;
    private static final boolean HALT_ON_ERROR_VALUE = true;
    private static final boolean HALT_ON_FAILURE_VALUE = true;

}
