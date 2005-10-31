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

package net.dpml.magic.tasks;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Policy;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.optional.junit.BatchTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Load a goal.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JUnitTestTask extends ProjectTask
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
    public static final String WORK_DIR_KEY = "project.dir"; // <--- CHANGE TO "project.test.dir"

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

        final File build = getContext().getBuildDirectory();
        final File src = new File( build, "test" );

        final File working = getContext().getTestDirectory();
        copyUnitTestResource( working );

        if( src.exists() )
        {
            final File classes = getContext().getTestClassesDirectory();
            mkDir( classes );

            final Definition definition = getDefinition();
            final Path classpath = definition.getPath( project, Policy.TEST );

            //
            // add the project jar to the classpath for the compilation
            // of the test classes and compile the test classes into the
            // target/test-classes directory
            //

            final File jar = getJarFile();
            if( jar.exists() )
            {
                classpath.createPathElement().setLocation( jar );
            }
            compile( src, classes, classpath );
            copyCompileResource( src, classes );

            //
            // add the test classes to the test classpath
            //

            classpath.createPathElement().setLocation( classes );
            test( src, classpath, working );
        }

        final String error = project.getProperty( ERROR_KEY );
        if( null != error )
        {
            final String message =
                "One or more unit test errors occured.";
            if( !getBooleanProperty( ReactorTask.REACTOR_HALT_ON_ERROR_KEY, true ) )
            {
                getIndex().getProject().setProperty( ReactorTask.REACTOR_TEST_ERRORS_KEY, "true" );
                project.log( 
                  message 
                  + ", IGNORED since " 
                  + ReactorTask.REACTOR_HALT_ON_ERROR_KEY
                  + "=" 
                  + getBooleanProperty( ReactorTask.REACTOR_HALT_ON_ERROR_KEY, true ) );
            }
            else
            {
                if( getHaltOnErrorProperty() )
                {
                    fail( message );
                }
            }
        }
        final String failure = project.getProperty( FAILURE_KEY );
        if( null != failure )
        {
            final String message =
                "One or more unit test failures occured.";
            if( !getBooleanProperty( ReactorTask.REACTOR_HALT_ON_FAILURE_KEY, ReactorTask.REACTOR_HALT_ON_ERROR_VALUE ) )
            {
                getIndex().getProject().setProperty( ReactorTask.REACTOR_TEST_FAILURES_KEY, "true" );
                project.log( 
                  message 
                  + ", IGNORED since " 
                  + ReactorTask.REACTOR_HALT_ON_FAILURE_KEY
                  + "=" 
                  + getBooleanProperty( ReactorTask.REACTOR_HALT_ON_FAILURE_KEY, true ) );
            }
            else
            {
                if( getHaltOnFailureProperty() )
                {
                    fail( message );
                }
            }
        }
    }

   /**
    * Return the projects deliverable jar file.
    * @return the jar file
    */ 
    public File getJarFile()
    {
        final File deliverables = getContext().getDeliverablesDirectory();
        final Definition def = getDefinition();
        final File types = new File( deliverables, "jars" );
        final String filename = def.getFilename( "jar" );
        return new File( types, filename );
    }

    private void copyUnitTestResource( final File dest )
    {
        final File build = getContext().getBuildDirectory();
        final File src = getUnitTestResourcesDirectory( build );
        if( src.exists() )
        {
            mkDir( dest );
            final Copy copy = (Copy) getProject().createTask( "copy" );
            copy.setTaskName( getTaskName() );
            copy.setPreserveLastModified( true );
            copy.setTodir( dest );

            final FileSet fileset = new FileSet();
            fileset.setDir( src );
            copy.addFileset( fileset );
            copy.init();
            copy.execute();
        }
    }

    private File getUnitTestResourcesDirectory( final File build )
    {
        final File etc = new File( build, "etc" );
        return new File( etc, "test" );
    }

    private void copyCompileResource( final File src, final File classes )
    {
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setPreserveLastModified( true );
        copy.setTodir( classes );

        final FileSet fileset = new FileSet();
        fileset.setDir( src );
        fileset.setIncludes( "**/**" );
        fileset.setExcludes( "**/*.java,**/package.html" );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }

    private void compile( final File sources, final File classes, final Path classpath )
    {
        final Javac javac = (Javac) getProject().createTask( "javac" );
        javac.setTaskName( getTaskName() );
        final Path src = javac.createSrc();
        final Path.PathElement element = src.createPathElement();
        element.setLocation( sources );
        javac.setDestdir( classes );
        javac.setDebug( getDebugProperty() );
        javac.setFork( getForkProperty() );
        javac.setClasspath( classpath );
        javac.init();
        javac.execute();
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

    private void test( final File src, final Path classpath, File working )
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

        final File reports = getContext().getTestReportsDirectory();
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
            return getCacheDirectory().toString();
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
