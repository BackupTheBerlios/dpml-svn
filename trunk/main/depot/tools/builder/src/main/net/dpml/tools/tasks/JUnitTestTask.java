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
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import net.dpml.library.Type;
import net.dpml.library.Resource;
import net.dpml.library.info.Scope;

import net.dpml.tools.Context;

import net.dpml.transit.Transit;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Exit;
import org.apache.tools.ant.taskdefs.optional.junit.BatchTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline;

/**
 * JUnit test execution.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class JUnitTestTask extends GenericTask
{
   /**
    * Constant for lookup of mx value.
    */
    public static final String MX_KEY = "project.test.mx";

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
    * Constant test fork mode key.
    */
    public static final String TEST_FORK_MODE_KEY = "project.test.fork.mode";

   /**
    * Constant test halt-on-error key (default is false).
    */
    public static final String HALT_ON_ERROR_KEY = "project.test.halt-on-error";

   /**
    * Constant test halt-on-failure key (default is false).
    */
    public static final String HALT_ON_FAILURE_KEY = "project.test.halt-on-failure";

   /**
    * Constant test abort on error key - if true (the default) the build will fail
    * if a test error occurs.
    */
    public static final String ABORT_ON_ERROR_KEY = "project.test.exit-on-error";

   /**
    * Constant test abort on failure key - if true (the default) the build will fail
    * if a test failure occurs.
    */
    public static final String ABORT_ON_FAILURE_KEY = "project.test.exit-on-failure";

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

   /**
    * the key for the exclude pattern for test cases
    */
    public static final String VERBOSE_KEY = "project.test.verbose";

    private static final String ERROR_KEY = "project.test.error";
    private static final String FAILURE_KEY = "project.test.failure";
    private static final String TEST_SRC_VALUE = "test";
    private static final String TEST_ENV_VALUE = "env";
    private static final boolean DEBUG_VALUE = true;
    private static final boolean FORK_VALUE = true;
    private static final boolean HALT_ON_ERROR_VALUE = false;
    private static final boolean HALT_ON_FAILURE_VALUE = false;

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
            getContext().getPath( Scope.TEST );
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
        if( !isTestingEnabled() )
        {
            return;
        }
        
        final Context context = getContext();
        final Project project = getProject();
        final File src = context.getTargetBuildTestDirectory();
        if( src.exists() )
        {
            final File working = context.getTargetTestDirectory();
            final Path classpath = getClasspath();
            
            executeUnitTests( src, classpath, working );
            
            if( getBooleanProperty( ABORT_ON_ERROR_KEY, true ) )
            {
                final String error = project.getProperty( ERROR_KEY );
                if( null != error )
                {
                    final String message =
                        "One or more unit test errors occured.";
                    fail( message );
                }
            }
            
            if( getBooleanProperty( ABORT_ON_FAILURE_KEY, true ) )
            {
                final String failure = project.getProperty( FAILURE_KEY );
                if( null != failure )
                {
                    final String message =
                        "One or more unit test failures occured.";
                    fail( message );
                }
            }
        }
    }
    
    private void executeUnitTests( final File src, final Path classpath, File working )
    {
        final Project project = getProject();
        log( "Test classpath: " + classpath, Project.MSG_VERBOSE );
        final FileSet fileset = createFileSet( src );
        final JUnitTask junit = (JUnitTask) project.createTask( "junit" );
        junit.setTaskName( getTaskName() );
        
        final JUnitTask.SummaryAttribute summary = getSummaryAttribute();
        junit.setPrintsummary( summary );
        
        junit.setShowOutput( true );
        junit.setTempdir( working );
        junit.setReloading( true );
        junit.setFiltertrace( true );
        
        junit.createClasspath().add( classpath );
        
        Context context = getContext();
        String verbose = getVerboseArgument();
        if( null != verbose )
        {
            Commandline.Argument arg = junit.createJvmarg();
            arg.setValue( "-verbose:" + verbose );
        }
            
        final File reports = getContext().getTargetReportsTestDirectory();
        mkDir( reports );

        final BatchTest batch = junit.createBatchTest();
        batch.addFileSet( fileset );
        batch.setTodir( reports );

        final FormatterElement plain = newConfiguredFormatter( "plain" );
        junit.addFormatter( plain );
        
        final FormatterElement xml = newConfiguredFormatter( "xml" );
        junit.addFormatter( xml );
        
        final Environment.Variable work = new Environment.Variable();
        work.setKey( WORK_DIR_KEY );
        work.setValue( working.toString() );
        junit.addConfiguredSysproperty( work );

        final Environment.Variable testBaseDir = new Environment.Variable();
        testBaseDir.setKey( "project.test.dir" );
        testBaseDir.setValue( working.toString() );
        junit.addConfiguredSysproperty( testBaseDir );
        
        final Environment.Variable targetDir = new Environment.Variable();
        targetDir.setKey( "project.target.dir" );
        targetDir.setValue( getContext().getTargetDirectory().toString() );
        junit.addConfiguredSysproperty( targetDir );

        final Environment.Variable deliverablesDir = new Environment.Variable();
        deliverablesDir.setKey( "project.target.deliverables.dir" );
        deliverablesDir.setValue( getContext().getTargetDeliverablesDirectory().toString() );
        junit.addConfiguredSysproperty( deliverablesDir );

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

        File base = project.getBaseDir();
        final File properties = new File( base, "test.properties" );
        if( properties.exists() )
        {
            Properties props = new Properties();
            try
            {
                InputStream input = new FileInputStream( properties );
                props.load( input );
                Enumeration enum = props.propertyNames();
                while( enum.hasMoreElements() )
                {
                    String name = (String) enum.nextElement();
                    final Environment.Variable v = new Environment.Variable();
                    v.setKey( name );
                    v.setValue( props.getProperty( name ) );
                    junit.addConfiguredSysproperty( v );
                }
            }
            catch( IOException ioe )
            {
                final String error = 
                  "Unexpected IO error while reading " + properties.toString();
                throw new BuildException( error, ioe, getLocation() );
            }
        }

        final File logProperties = new File( working, "logging.properties" );
        if( logProperties.exists() )
        {
            final Environment.Variable log = new Environment.Variable();
            log.setKey( "dpml.logging.config" );
            try
            {
                log.setValue( logProperties.toURL().toString() );
            }
            catch( IOException e )
            {
                final String error = 
                  "Unexpected file to url error."
                  + "\nFile: " + logProperties;
                throw new BuildException( error, e );
            }
            junit.addConfiguredSysproperty( log );
        }
        
        String formatter = getResource().getProperty( "java.util.logging.config.class" );
        if( null != formatter )
        {
            final Environment.Variable logging = new Environment.Variable();
            logging.setKey( "java.util.logging.config.class" );
            if( "dpml".equals( formatter ) )
            {
                logging.setValue( "net.dpml.util.ConfigurationHandler" );
            }
            else
            {
                logging.setValue( formatter );
            }
            junit.addConfiguredSysproperty( logging );
        }
        
        final Environment.Variable endorsed = new Environment.Variable();
        endorsed.setKey( "java.endorsed.dirs" );
        endorsed.setValue( new File( Transit.DPML_SYSTEM, "lib/endorsed" ).getAbsolutePath() );
        junit.addConfiguredSysproperty( endorsed );
        
        configureDeliverableSysProperties( junit );
        configureForExecution( junit );
        
        junit.setErrorProperty( ERROR_KEY );
        junit.setFailureProperty( FAILURE_KEY );
        final boolean haltOnErrorPolicy = getHaltOnErrorPolicy();
        if( haltOnErrorPolicy )
        {
            junit.setHaltonerror( true );
        }
        final boolean haltOnFailurePolicy = getHaltOnFailurePolicy();
        if( haltOnFailurePolicy )
        {
            junit.setHaltonfailure( true );
        }
        
        junit.init();
        junit.execute();
    }
    
    private void configureForExecution( JUnitTask task )
    {
        if( getForkProperty() )
        {
            task.setFork( true );
            Project project = getProject();
            task.setDir( project.getBaseDir() );
            JUnitTask.ForkMode mode = getForkMode();
            if( null == mode )
            {
                log( "Executing forked test." );
            }
            else
            {
                log( "Executing forked test with mode: '" + mode + "'." );
                task.setForkMode( mode );
            }
            String mx = getContext().getProperty( MX_KEY );
            if( null != mx )
            {
                task.setMaxmemory( mx );
            }
        }
        else
        {
            log( "executing in local jvm" );
            JUnitTask.ForkMode mode = new JUnitTask.ForkMode( "once" );
            task.setForkMode( mode );
            task.setFork( false );
        }
    }
    
    private void configureDeliverableSysProperties( JUnitTask task )
    {
        try
        {
            Context context = getContext();
            Resource resource = context.getResource();
            Type[] types = context.getResource().getTypes();
            for( int i=0; i<types.length; i++ )
            {
                Type type = types[i];
                String id = type.getID();
                File file = context.getTargetDeliverable( id );
                String path = file.getCanonicalPath();
                final Environment.Variable variable = new Environment.Variable();
                variable.setKey( "project.deliverable." + id + ".path" );
                variable.setValue( path );
                task.addConfiguredSysproperty( variable );
            }
        }
        catch( IOException ioe )
        {
            final String error = 
              "Unexpected IO error while building deliverable filename properties.";
            throw new BuildException( error, ioe, getLocation() );
        }
    }

    private FileSet createFileSet( File src )
    {
        final FileSet fileset = new FileSet();
        fileset.setDir( src );
        addIncludes( fileset );
        addExcludes( fileset );
        return fileset;
    }

    private void addIncludes( FileSet set )
    {
        String pattern = getTestIncludes();
        log( "Test includes=" + pattern, Project.MSG_VERBOSE );
        StringTokenizer tokenizer = new StringTokenizer( pattern, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String item = tokenizer.nextToken();
            set.createInclude().setName( item );
        }
    }

    private void addExcludes( FileSet set )
    {
        String pattern = getTestExcludes();
        log( "Test excludes=" + pattern, Project.MSG_VERBOSE );
        StringTokenizer tokenizer = new StringTokenizer( pattern, ", ", false );
        while( tokenizer.hasMoreTokens() )
        {
            String item = tokenizer.nextToken();
            set.createExclude().setName( item );
        }
    }

    private String getTestIncludes()
    {
        String includes = getContext().getProperty( TEST_INCLUDES_KEY );
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
        String excludes = getContext().getProperty( TEST_EXCLUDES_KEY );
        if( null != excludes )
        {
            return excludes;
        }
        else
        {
            return TEST_EXCLUDES_VALUE;
        }
    }

    private String getCachePath()
    {
        final String value = getContext().getProperty( CACHE_PATH_KEY );
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

    private boolean getForkProperty()
    {
        return getBooleanProperty( FORK_KEY, FORK_VALUE );
    }

    private JUnitTask.ForkMode getForkMode()
    {
        final String value = getContext().getProperty( TEST_FORK_MODE_KEY );
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
        final String value = getContext().getProperty( key );
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

    private boolean isTestingEnabled()
    {
        final String enabled = getContext().getProperty( TEST_ENABLED_KEY, "true" );
        return "true".equals( enabled );
    }

    private JUnitTask.SummaryAttribute getSummaryAttribute()
    {
        final Project project = getProject();
        final JUnitTask.SummaryAttribute summary = new JUnitTask.SummaryAttribute();
        summary.setValue( "on" );
        return summary;
    }
    
    private boolean getHaltOnErrorPolicy()
    {
        return getBooleanProperty(
            HALT_ON_ERROR_KEY, HALT_ON_ERROR_VALUE );
    }
    
    private boolean getHaltOnFailurePolicy()
    {
        return getBooleanProperty(
            HALT_ON_FAILURE_KEY, HALT_ON_FAILURE_VALUE );
    }
    
    private String getVerboseArgument()
    {
        Context context = getContext();
        return context.getProperty( "project.test.verbose" );
    }
    
    private FormatterElement newConfiguredFormatter( String type )
    {
        final FormatterElement formatter = new FormatterElement();
        final FormatterElement.TypeAttribute attribute = new FormatterElement.TypeAttribute();
        attribute.setValue( type );
        formatter.setType( attribute );
        return formatter;
    }
    
}
