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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * Compile sources located in ${project.target}/main to java class file under
 * the ${project.target}/classes directory.  Properties influencing the compilation
 * include:
 * <ul>
 *  <li>project.javac.debug : boolean true (default) or false</li>
 *  <li>project.javac.fork: boolean true or false (default) </li>
 *  <li>project.javac.deprecation: boolean true (default) or false</li>
 * </ul>
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JavacTask extends MatchingTask
{
   /**
    * Constant debug key.
    */ 
    public static final String DEBUG_KEY = "project.javac.debug";

   /**
    * Constant fork key.
    */ 
    public static final String FORK_KEY = "project.javac.fork";

   /**
    * Constant deprecation warning key.
    */ 
    public static final String DEPRECATION_KEY = "project.javac.deprecation";

   /**
    * Constant src directory key.
    */ 
    public static final String SOURCE_KEY = "project.javac.source";

   /**
    * Constant classes target directory key.
    */ 
    public static final String TARGET_KEY = "project.javac.target";

    private static final boolean DEBUG_VALUE = false;
    private static final boolean FORK_VALUE = false;
    private static final boolean DEPRECATION_VALUE = true;

    private String m_classPathRef;
    private Path m_classPath;
    private File m_destination;
    private File m_source;
    private boolean m_init = false;
    
   /**
    * Task initialization.
    * @exception BuildException if a initialization failure occurs
    */
    public void init() throws BuildException
    {
        if( !m_init )
        {
            super.init();
            final Project project = getProject();
            project.setProperty( DEBUG_KEY, "" + DEBUG_VALUE );
            project.setProperty( FORK_KEY, "" + FORK_VALUE );
            m_init = true;
        }
    }
    
   /**
    * Set the compilation classpath.
    * @param path the classpath
    */
    public void setClasspath( Path path ) 
    {
        m_classPath = path;
    }
    
   /**
    * Set the id of the compilation classpath.
    * @param id the classpath reference
    */
    public void setClasspathRef( String id ) 
    {
        m_classPathRef = id;
    }
    
    public void setDest( File destination )
    {
        m_destination = destination;
    }
    
    public void setSrc( File source )
    {
        m_source = source;
    }
    
   /**
    * Task execution.
    */
    public void execute()
    {
        if( null == m_destination )
        {
            final String error = 
              "Missing 'dest' attribute.";
            throw new BuildException( error, getLocation() );
        }
        if( null == m_source )
        {
            final String error = 
              "Missing 'src' attribute.";
            throw new BuildException( error, getLocation() );
        }
        
        if( !m_source.exists() )
        {
            return;
        }
        
        final Path classpath = getClasspath();
        final Project project = getProject();
        mkDir( m_destination );
        final Javac javac = (Javac) getProject().createTask( "javac" );
        javac.setTaskName( getTaskName() );
        javac.setIncludeantruntime( false );
        String lint = getProject().getProperty( "project.javac.lint" );
        if( null != lint )
        {
            javac.createCompilerArg().setValue( "-Xlint:" + lint );
        }
        
        final Path srcDirPath = new Path( project );
        srcDirPath.createPathElement().setLocation( m_source );
        javac.setSrcdir( srcDirPath );
        
        final Path srcPath = new Path( project );
        FileSet fileset = super.getImplicitFileSet();
        fileset.setDir( m_source );
        javac.setSourcepath( srcPath );
        
        javac.setDestdir( m_destination );
        javac.setDeprecation( getDeprecationProperty() );
        javac.setDebug( getDebugProperty() );
        javac.setFork( getForkProperty() );
        javac.setSource( getSourceProperty() );
        javac.setTarget( getTargetProperty() );
        javac.setClasspath( classpath );
        javac.init();
        javac.execute();
        
        copy( m_source, m_destination, false, "**/*.*", "**/*.java" );

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
              "Missing 'classpathRef' or 'classpath' attribute.";
            throw new BuildException( error, getLocation() );
        }
    }
    
    /*
    private void compile( final File sources, final File classes, final Path classpath )
    {
        final Javac javac = (Javac) getProject().createTask( "javac" );
        javac.setTaskName( getTaskName() );
        javac.setIncludeantruntime( false );
        String lint = getProject().getProperty( "project.javac.lint" );
        if( null != lint )
        {
            javac.createCompilerArg().setValue( "-Xlint:" + lint );
        }
        final Path src = javac.createSrc();
        final Path.PathElement element = src.createPathElement();
        element.setLocation( sources );
        javac.setDestdir( classes );
        javac.setDeprecation( getDeprecationProperty() );
        javac.setDebug( getDebugProperty() );
        javac.setFork( getForkProperty() );
        javac.setSource( getSourceProperty() );
        javac.setTarget( getTargetProperty() );
        javac.setClasspath( classpath );
        javac.init();
        javac.execute();
    }
    */

    private boolean getDebugProperty()
    {
        return getBooleanProperty( DEBUG_KEY, DEBUG_VALUE );
    }

    private boolean getDeprecationProperty()
    {
        return getBooleanProperty( DEPRECATION_KEY, DEPRECATION_VALUE );
    }

    private boolean getForkProperty()
    {
        return getBooleanProperty( FORK_KEY, FORK_VALUE );
    }

    private String getSourceProperty()
    {
        return getProject().getProperty( SOURCE_KEY );
    }

    private String getTargetProperty()
    {
        return getProject().getProperty( TARGET_KEY );
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

    protected void mkDir( final File dir )
    {
        final Mkdir mkdir = (Mkdir) getProject().createTask( "mkdir" );
        mkdir.setTaskName( getTaskName() );
        mkdir.setDir( dir );
        mkdir.init();
        mkdir.execute();
    }

    protected void copy(
       final File src, final File destination, final boolean filtering, final String includes, final String excludes )
    {
        mkDir( destination );
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setTodir( destination );
        copy.setFiltering( filtering );
        copy.setOverwrite( false );
        copy.setPreserveLastModified( true );
        final FileSet fileset = new FileSet();
        fileset.setDir( src );
        fileset.setIncludes( includes );
        fileset.setExcludes( excludes );
        copy.addFileset( fileset );
        copy.init();
        copy.execute();
    }

}
