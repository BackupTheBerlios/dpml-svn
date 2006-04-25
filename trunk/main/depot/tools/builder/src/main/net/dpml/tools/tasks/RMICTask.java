/*
 * Copyright 2006 Stephen McConnell
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

import net.dpml.library.Resource;
import net.dpml.library.info.RMICDirective;
import net.dpml.library.info.IncludePatternDirective;
import net.dpml.library.info.ExcludePatternDirective;
import net.dpml.library.info.Scope;

import net.dpml.tools.model.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Rmic;
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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RMICTask extends GenericTask
{
    private final Context m_context;
    private final RMICDirective m_data;
    private String m_classPathRef;
    private File m_base;
   
   /**
    * Creation of a new RMICTask.
    * @param context the project context
    */
    public RMICTask( Context context, RMICDirective directive )
    {
        super();
        m_context = context;
        m_data = directive;
        try
        {
            setProject( context.getProject() );
            setTaskName( "rmic" );
            setBase( context.getTargetClassesMainDirectory() );
            context.getPath( Scope.RUNTIME );
            setClasspathRef( "project.compile.path" );
        }
        catch( Exception e )
        {
            throw new BuildException( e );
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
    * Set the destination directory.
    * @param destination the destination directory
    */
    public void setBase( File base )
    {
        m_base = base;
    }
    
   /**
    * Task execution.
    */
    public void execute()
    {
        if( null == m_base )
        {
            final String error = 
              "Missing 'base' argument.";
            throw new BuildException( error, getLocation() );
        }
        
        if( !m_base.exists() )
        {
            return;
        }
        
        final Rmic rmic = (Rmic) getProject().createTask( "rmic" );
        rmic.setTaskName( "rmic" );
        final Project project = m_context.getProject();
        rmic.setProject( project );
        rmic.setBase( m_base );
        final Path classpath = getClasspath();
        rmic.setClasspath( classpath );
        
        // populate includes/excludes
        
        IncludePatternDirective[] includes = m_data.getIncludes();
        for( int i=0; i<includes.length; i++ )
        {
            IncludePatternDirective include = includes[i];
            rmic.createInclude().setName( include.getName() );
        }
        
        ExcludePatternDirective[] excludes = m_data.getExcludes();
        for( int i=0; i<excludes.length; i++ )
        {
            ExcludePatternDirective exclude = excludes[i];
            rmic.createExclude().setName( exclude.getName() );
        }
        
        // initialize and execute
        
        rmic.init();
        rmic.execute();
    }
    
    private Path getClasspath()
    {
        if( null != m_classPathRef )
        {
            return (Path) getProject().getReference( m_classPathRef );
        }
        else
        {
            final String error = 
              "Missing 'classpathRef' attribute.";
            throw new BuildException( error, getLocation() );
        }
    }
}
