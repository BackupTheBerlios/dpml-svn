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

import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Policy;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * Compile sources.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JavacTask extends SystemTask
{

    public static final String DEBUG_KEY = "java.compile.debug";
    public static final boolean DEBUG_VALUE = true;

    public static final String FORK_KEY = "java.compile.fork";
    public static final boolean FORK_VALUE = false;

    public static final String DEPRECATION_KEY = "java.compile.deprecation";
    public static final boolean DEPRECATION_VALUE = true;

    public void init() throws BuildException 
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty( DEBUG_KEY, "" + DEBUG_VALUE );
            project.setNewProperty( FORK_KEY, "" + FORK_VALUE );
        }
    }

    public void execute() throws BuildException 
    {
        final Project project = getProject();
        final File build = getContext().getBuildDirectory();
        final File main = new File( build, Context.SRC_MAIN );

        if( main.exists() )
        {
            final File classes = getContext().getClassesDirectory();
            mkDir( classes );

            final ResourceRef ref = new ResourceRef( getKey() );
            final Definition definition = getHome().getDefinition( ref );
            final Path classpath = definition.getPath( project, Policy.BUILD );

            compile( main, classes, classpath );

            final Copy copy = (Copy) getProject().createTask( "copy" );
            copy.setTaskName( getTaskName() );
            copy.setPreserveLastModified( true );
            copy.setTodir( classes );

            final FileSet fileset = new FileSet();
            fileset.setDir( main );
            fileset.setIncludes( "**/**" );
            fileset.setExcludes( "**/*.java,**/package.html" );
            copy.addFileset( fileset );
            copy.init();
            copy.execute();

            copyMainResource( classes );
        }
        else
        {
            log( "no src main", Project.MSG_VERBOSE );
        }
    }

    private void copyMainResource( final File dest )
    {
        final File build = getContext().getBuildDirectory();
        final File etc = new File( build, "etc" );
        final File src = new File( etc, "main" );
        if( src.exists() )
        {
            mkDir( dest );
            final Copy copy = (Copy) getProject().createTask( "copy" );
            copy.setPreserveLastModified( true );
            copy.setTodir( dest );

            final FileSet fileset = new FileSet();
            fileset.setDir( src );
            copy.addFileset( fileset );
            copy.init();
            copy.execute();
        }
    }

    private void compile( final File sources, final File classes, final Path classpath )
    {
        final Javac javac = (Javac) getProject().createTask( "javac" );
        javac.setTaskName( getTaskName() );
        final Path src = javac.createSrc();
        final Path.PathElement element = src.createPathElement();
        element.setLocation( sources );
        javac.setDestdir( classes );
        javac.setDeprecation( getDeprecationProperty() );
        javac.setDebug( getDebugProperty() );
        javac.setFork( getForkProperty() );
        javac.setClasspath( classpath );
        javac.init();
        javac.execute();
    }

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
}
